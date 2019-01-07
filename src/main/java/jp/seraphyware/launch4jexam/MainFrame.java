package jp.seraphyware.launch4jexam;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

	private String[] args;

	public MainFrame(String[] args) {
		try {
			this.args = args;

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
			table.setRowHeight((int)(table.getFont().getSize() * 1.2));
		}

		public void setKeyValueMap(Map<String, String> map) {
			model.setKeyValueMap(map);
		}
	}

	private ScaleSupport scaleSupport;

	private TitledKeyValuePanel argsPanel;

	private TitledKeyValuePanel scalePanel;

	private TitledKeyValuePanel propPanel;

	private TitledKeyValuePanel envPanel;

	private void initLayout() {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());

		scalePanel = new TitledKeyValuePanel("ScreenScale");
		propPanel = new TitledKeyValuePanel("System Properties");
		envPanel = new TitledKeyValuePanel("Environments");
		argsPanel = new TitledKeyValuePanel("Command Line Arguments");

		scaleSupport = ScaleSupport.getInstance(this);

		argsPanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 150)));
		scalePanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 150)));
		propPanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 200)));
		envPanel.setPreferredSize(scaleSupport.manualScaled(new Dimension(400, 200)));

		JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scalePanel, propPanel);
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane1, envPanel);
		JSplitPane splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane2, argsPanel);
		splitPane1.setDividerLocation(0.5);
		splitPane2.setDividerLocation(0.66);
		splitPane3.setDividerLocation(0.75);

		container.add(splitPane3, BorderLayout.CENTER);

		pack();
	}

	public void load() {
		loadScale();
		loadSysProps();
		loadEnv();
		loadArgs();
	}

	public void loadScale() {
		Map<String, String> scaleMap = new LinkedHashMap<>();
		scaleMap.put("Java Version", Double.toString(JavaVersionUtils.getJavaVersion()));
		scaleMap.put("System Scale x", Double.toString(scaleSupport.getDefaultScaleX()));
		scaleMap.put("System Scale y", Double.toString(scaleSupport.getDefaultScaleY()));
		scaleMap.put("Retina", Boolean.toString(scaleSupport.isRetina()));
		scaleMap.put("Calubrate Scale x", Double.toString(scaleSupport.getManualScaleX()));
		scaleMap.put("Calubrate Scale y", Double.toString(scaleSupport.getManualScaleY()));
		scaleMap.put("Resolution", Integer.toString(ScaleSupport.getScreenResolution()));
		scaleMap.put("Compute Scale", Float.toString(ScaleSupport.getScreenScale()));
		scalePanel.setKeyValueMap(scaleMap);
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

	public void loadArgs() {
		Map<String, String> argsMap = new LinkedHashMap<>();
		RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
		List<String> vmArgs = runtimeMxBean.getInputArguments();

		int idx = 0;
		for (String arg : vmArgs) {
			argsMap.put("jvm" + Integer.toString(++idx), arg);
		}

		idx = 0;
		for (String arg : args) {
			argsMap.put("app" + Integer.toString(++idx), arg);
		}

		argsPanel.setKeyValueMap(argsMap);
	}

	protected void onClose() {
		dispose();
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SwingUtilities.invokeLater(() -> {
			MainFrame main = new MainFrame(args);
			main.setLocationByPlatform(true);
			main.setVisible(true);
		});
	}
}
