package jp.seraphyware.launch4jexam;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class MainFrame extends JFrame {


	public MainFrame() {
		try {
			setTitle(getClass().getCanonicalName());
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					onClose();
				}
			});
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			initLayout();
			load();

		} catch (RuntimeException ex) {
			dispose();
			throw ex;
		}
	}

	private static class KeyValueTableModel extends AbstractTableModel {

		private enum ColumnDef {
			KEY(200) {
				@Override
				public String getValue(Entry<String, String> entry) {
					return entry.getKey();
				}
			},
			VALUE(400) {
				@Override
				public String getValue(Entry<String, String> entry) {
					return entry.getValue();
				}
			};

			private final int width;

			ColumnDef(int width) {
				this.width = width;
			}

			public abstract String getValue(Map.Entry<String, String> entry);

			public int getWidth() {
				return width;
			}
		}

		private static final ColumnDef[] COLUMNS = ColumnDef.values();

		private List<Map.Entry<String, String>> keyValueEntries = Collections.emptyList();

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNS[column].name();
		}

		@Override
		public int getRowCount() {
			return keyValueEntries.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Map.Entry<String, String> entry = keyValueEntries.get(rowIndex);
			return COLUMNS[columnIndex].getValue(entry);
		}

		public void adjustColumns(JTable table) {
			TableColumnModel columnModel = table.getColumnModel();
			int mx = columnModel.getColumnCount();
			for (int colIdx = 0; colIdx < mx; colIdx++) {
				TableColumn column = columnModel.getColumn(colIdx);
				int width = COLUMNS[colIdx].getWidth();
				column.setPreferredWidth(width);
			}
		}

		public void setKeyValueMap(Map<String, String> map) {
			this.keyValueEntries = new ArrayList<>(map.entrySet());
			fireTableDataChanged();
		}
	}

	private static class TitledKeyValuePanel extends JPanel {

		private final KeyValueTableModel model = new KeyValueTableModel();

		private final JTable table = new JTable(model);

		public TitledKeyValuePanel(String title) {
			setLayout(new BorderLayout());
			setBorder(new TitledBorder(title));
			add(new JScrollPane(table), BorderLayout.CENTER);

			table.setAutoCreateRowSorter(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			model.adjustColumns(table);

			// フォントサイズで行の高さを設定する
			table.setRowHeight(table.getFont().getSize());
		}

		public void setKeyValueMap(Map<String, String> map) {
			model.setKeyValueMap(map);
		}
	}

	private TitledKeyValuePanel propPanel;

	private TitledKeyValuePanel envPanel;

	private void initLayout() {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());

		propPanel = new TitledKeyValuePanel("System Properties");
		envPanel = new TitledKeyValuePanel("Environments");

		ScaleSupport scaleSupport = ScaleSupport.getInstance(this);
		propPanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 200)));
		envPanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 200)));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, propPanel, envPanel);
		splitPane.setDividerLocation(0.5);

		container.add(splitPane, BorderLayout.CENTER);

		pack();
	}

	public void load() {
		loadSysProps();
		loadEnv();
	}

	public void loadSysProps() {
		Properties props = System.getProperties();
		Map<String, String> propMap = new TreeMap<>();
		for (String key : props.stringPropertyNames()) {
			String value = props.getProperty(key);
			propMap.put(key, value);
		}
		propPanel.setKeyValueMap(propMap);
	}

	public void loadEnv() {
		Map<String, String> envMap = new TreeMap<>(System.getenv());
		envPanel.setKeyValueMap(envMap);
	}

	protected void onClose() {
		dispose();
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SwingUtilities.invokeLater(() -> {
			MainFrame main = new MainFrame();
			main.setLocationByPlatform(true);
			main.setVisible(true);
		});
	}
}
