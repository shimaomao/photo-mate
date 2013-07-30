package nl.alexeyu.photomate.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import nl.alexeyu.photomate.model.Photo;
import nl.alexeyu.photomate.service.UpdateListener;

public class KeywordPicker {
	
	private JPanel panel;
	
	private JTextField keywordText;
	
	private JList<String> recommendedKeywordList = new JList<>();
	
	private JList<String> actualKeywordList  = new JList<>();
	
	private JButton addKeywordButton;
	
	private JButton removeKeywordButton;
	
	private UpdateListener<String> addKeywordListener;
	
	private UpdateListener<String> removeKeywordListener;
	
	public KeywordPicker() {
		build();
	}
	
	private void build() {
		panel = new JPanel(new BorderLayout(5, 5));

		JPanel textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.X_AXIS));
		JLabel label = new JLabel("Keyword: ");
		textPane.add(label);
		textPane.add(Box.createRigidArea(new Dimension(5,0)));
		keywordText = new JTextField();
		textPane.add(keywordText);
		
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.X_AXIS));
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		buttonPane.setBorder(Constants.EMPTY_BORDER);
		addKeywordButton = new JButton(new ImageIcon(getClass().getResource("/img/plus.png")));
		addKeywordButton.addActionListener(new AddKeywordTask());
		removeKeywordButton = new JButton(new ImageIcon(getClass().getResource("/img/minus.png"))); 
		removeKeywordButton.addActionListener(new RemoveKeywordTask());
		buttonPane.add(addKeywordButton);
		buttonPane.add(Box.createRigidArea(new Dimension(0,5)));
		buttonPane.add(removeKeywordButton);
		
		listPane.add(new JScrollPane(recommendedKeywordList), BorderLayout.WEST);
		listPane.add(buttonPane, BorderLayout.CENTER);
		listPane.add(new JScrollPane(actualKeywordList), BorderLayout.EAST);
		
		panel.add(textPane, BorderLayout.NORTH);
		panel.add(listPane);
	}
	
	public void setPhoto(Photo photo) {
		ListModel<String> listModel = new KeywordListModel(photo);
		actualKeywordList.setModel(listModel);
		actualKeywordList.revalidate();
		actualKeywordList.repaint();
	}
	
	public JComponent getComponent() {
		return panel;
	}

	private static class KeywordListModel extends AbstractListModel<String> {
		
		private final Photo photo;
		
		public KeywordListModel(Photo photo) {
			this.photo = photo;
		}

		public int getSize() {
			return photo.getKeywords().size();
		}

		public String getElementAt(int index) {
			return photo.getKeywords().get(index);
		}
		
	}
	
	public void onKeywordAdd(String keyword) {
		if (addKeywordListener != null) {
			addKeywordListener.onUpdate(keyword);
		}
	}
	
	public void onKeywordRemove(String keyword) {
		if (removeKeywordListener != null) {
			removeKeywordListener.onUpdate(keyword);
		}
	}

	public void setAddKeywordListener(UpdateListener<String> addKeywordListener) {
		this.addKeywordListener = addKeywordListener;
	}

	public void setRemoveKeywordListener(
			UpdateListener<String> removeKeywordListener) {
		this.removeKeywordListener = removeKeywordListener;
	}

	private class AddKeywordTask implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String keyword = (String) recommendedKeywordList.getSelectedValue();
			if (keyword == null) {
				keyword = keywordText.getText();
			}
			if (keyword != null) {
				ListModel<String> model = actualKeywordList.getModel();
				for (int i = 0; i < model.getSize(); i++) {
					if (keyword.equalsIgnoreCase(model.getElementAt(i).toString())) {
						return;
					}
				}
				onKeywordAdd(keyword);
			}
		}
		
	}
	
	private class RemoveKeywordTask implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String keyword = (String) actualKeywordList.getSelectedValue();
			if (keyword != null) {
				onKeywordRemove(keyword);
			}
		}
		
	}

}
