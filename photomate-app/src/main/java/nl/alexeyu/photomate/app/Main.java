package nl.alexeyu.photomate.app;

import static nl.alexeyu.photomate.ui.UiConstants.BORDER_WIDTH;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.alexeyu.photomate.api.AbstractPhoto;
import nl.alexeyu.photomate.api.editable.EditablePhoto;
import nl.alexeyu.photomate.service.EditablePhotoManager;
import nl.alexeyu.photomate.service.PhotoNotReadyException;
import nl.alexeyu.photomate.service.archive.PhotoArchiver;
import nl.alexeyu.photomate.service.upload.PhotoUploader;
import nl.alexeyu.photomate.ui.ArchivePhotoContainer;
import nl.alexeyu.photomate.ui.DirChooser;
import nl.alexeyu.photomate.ui.EditablePhotoContainer;
import nl.alexeyu.photomate.ui.EditablePhotoMetaDataPanel;
import nl.alexeyu.photomate.ui.PhotoContainer;
import nl.alexeyu.photomate.ui.ReadonlyPhotoMetaDataPanel;
import nl.alexeyu.photomate.ui.StockPhotoContainer;
import nl.alexeyu.photomate.ui.UiConstants;
import nl.alexeyu.photomate.ui.UploadPanel;
import nl.alexeyu.photomate.util.ConfigReader;

public class Main implements PropertyChangeListener {

    private JFrame frame = new JFrame("Your Photo Mate");

    private static final String DEFAULT_FOLDER_PROPERTY = "defaultFolder";

    private static final String SHUTTERSTOCK_SOURCE = "Shutterstock";
    private static final String LOCAL_SOURCE = "Local";

    private JButton uploadButton = new JButton();

    private EditablePhotoMetaDataPanel photoMetaDataPanel = new EditablePhotoMetaDataPanel();

    private ReadonlyPhotoMetaDataPanel sourcePhotoMetaDataPanel = new ReadonlyPhotoMetaDataPanel(
            photoMetaDataPanel.getDropTarget());

    private ExternalPhotoContainerRegistry photoSourceRegistry = new ExternalPhotoContainerRegistry();

    private DirChooser dirChooser;

    @Inject
    private EditablePhotoManager photoManager;

    @Inject
    private EditablePhotoContainer editablePhotoContainer;

    @Inject
    private StockPhotoContainer stockPhotoContainer;

    @Inject
    private ArchivePhotoContainer archivePhotoContainer;

    @Inject
    private ConfigReader configReader;

    @Inject
    private PhotoUploader photoUploader;

    @Inject
    private EventBus eventBus;

    public void start() {
        registerPhotoSources();
        dirChooser = new DirChooser(configReader.getProperty(DEFAULT_FOLDER_PROPERTY).orElse(null));
        initListeners();
        buildGraphics();
        dirChooser.init();
        activateWindow();
    }

    private void activateWindow() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1300, 800);
        frame.setVisible(true);
    }

    private void registerPhotoSources() {
        photoSourceRegistry.registerPhotoSource(LOCAL_SOURCE, archivePhotoContainer);
        photoSourceRegistry.registerPhotoSource(SHUTTERSTOCK_SOURCE, stockPhotoContainer);
    }

    private void initListeners() {
        editablePhotoContainer.addPhotoObserver(photoMetaDataPanel);
        editablePhotoContainer.addPhotoObserver(photoManager);
        dirChooser.addPropertyChangeListener(DirChooser.DIR_PROPERTY, this);
        photoMetaDataPanel.addPropertyChangeListener(photoManager);
        sourcePhotoMetaDataPanel.addPropertyChangeListener(photoManager);
        stockPhotoContainer.addPhotoObserver(sourcePhotoMetaDataPanel);
        archivePhotoContainer.addPhotoObserver(sourcePhotoMetaDataPanel);
    }

    private void buildGraphics() {
        JPanel centerPanel = new JPanel(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
        centerPanel.setBorder(UiConstants.EMPTY_BORDER);
        centerPanel.add(prepareCurrentPhotoPanel(), BorderLayout.WEST);
        centerPanel.add(prepareSourcePhotosPanel(), BorderLayout.CENTER);

        frame.getContentPane().add(prepareLocalPhotosPanel(), BorderLayout.WEST);
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
    }

    private JComponent prepareLocalPhotosPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(dirChooser, BorderLayout.NORTH);
        p.add(editablePhotoContainer, BorderLayout.CENTER);
        return p;
    }

    private JComponent prepareCurrentPhotoPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(UiConstants.EMPTY_BORDER);
        p.add(editablePhotoContainer.getPreview(), BorderLayout.NORTH);
        p.add(photoMetaDataPanel, BorderLayout.CENTER);
        return p;
    }

    private JComponent prepareSourcePhotosPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(UiConstants.EMPTY_BORDER);
        final CardLayout sourcesLayout = new CardLayout();
        final JPanel sourcesPanel = new JPanel(sourcesLayout);
        sourcesPanel.setPreferredSize(UiConstants.PREVIEW_SIZE);

        final ButtonGroup bgroup = new ButtonGroup();
        ActionListener l = new ChangePhotoListener(sourcesPanel, bgroup, sourcesLayout);

        JPanel buttonsPanel = new JPanel();
        BoxLayout buttonsLayout = new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS);
        buttonsPanel.setLayout(buttonsLayout);
        buttonsPanel.setBorder(UiConstants.EMPTY_BORDER);

        buttonsPanel.add(new JLabel("Source:"));
        buttonsPanel.add(Box.createVerticalStrut(5));
        for (String sourceName : photoSourceRegistry.getSourceNames()) {
            sourcesPanel.add(photoSourceRegistry.getPhotoSource(sourceName), sourceName);
            sourcesPanel.add(archivePhotoContainer, LOCAL_SOURCE);
            JRadioButton rb = new JRadioButton(sourceName);
            rb.setActionCommand(sourceName);
            rb.addActionListener(l);
            rb.setSelected(bgroup.getButtonCount() == 0);
            bgroup.add(rb);
            buttonsPanel.add(rb);
        }
        l.actionPerformed(null);
        uploadButton.addActionListener(new UploadStarter());
        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(uploadButton);

        JPanel centerPanel = new JPanel(new BorderLayout(BORDER_WIDTH, BORDER_WIDTH));
        centerPanel.add(sourcePhotoMetaDataPanel, BorderLayout.WEST);
        centerPanel.add(buttonsPanel, BorderLayout.CENTER);

        p.add(sourcesPanel, BorderLayout.NORTH);
        p.add(centerPanel, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getNewValue() == null) {
            return;
        }
        Path dir = Paths.get(e.getNewValue().toString());
        if (Files.exists(dir)) {
            List<EditablePhoto> photos = photoManager.createPhotos(dir);
            editablePhotoContainer.setPhotos(photos);
            uploadButton.setText("Upload [" + photos.size() + "]");
        }
    }

    private final class ChangePhotoListener implements ActionListener {
        private final JPanel sourcesPanel;
        private final ButtonGroup bgroup;
        private final CardLayout sourcesLayout;

        private ChangePhotoListener(JPanel sourcesPanel, ButtonGroup bgroup, CardLayout sourcesLayout) {
            this.sourcesPanel = sourcesPanel;
            this.bgroup = bgroup;
            this.sourcesLayout = sourcesLayout;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            String sourceName = bgroup.getSelection().getActionCommand();
            sourcesLayout.show(sourcesPanel, sourceName);
            PhotoContainer<AbstractPhoto> photoContainer = (PhotoContainer<AbstractPhoto>) photoSourceRegistry
                    .getPhotoSource(sourceName);
            sourcePhotoMetaDataPanel.setPhoto(photoContainer.getSelectedPhoto().orElse(null));
        }
    }

    private class UploadStarter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                List<EditablePhoto> photos = photoManager.validatePhotos();
                UploadPanel uploadPanel = new UploadPanel(photos, configReader.getPhotoStocks());
                eventBus.register(uploadPanel);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new JScrollPane(uploadPanel));
                frame.revalidate();
                frame.repaint();
                SwingUtilities.invokeLater(() -> photoUploader.uploadPhotos(photos));
            } catch (PhotoNotReadyException ex) {
                JOptionPane.showMessageDialog(frame, "Cannot upload photos: " + ex.getPhotos());
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new AppModule());
        PhotoArchiver photoArchiver = new PhotoArchiver();
        injector.injectMembers(photoArchiver);
        injector.getInstance(EventBus.class).register(photoArchiver);
        injector.getInstance(Main.class).start();
    }

}
