package jp.seraphyware.launch4jexam;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

/**
 * スクリーンのスケールを取得する。
 *
 * Java8であれば自動スケールがかかっていないので、マニュアルでスケール倍して座標を補正する。
 * Java11であれば自動スケールがかかっている。
 */
public class ScaleSupport {

	private static final float scale;

	private double scaleX;

	private double scaleY;

	private double manualScaleX;

	private double manualScaleY;

	static {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int resolution = tk.getScreenResolution();
		System.out.println("Screen Resolution: " + resolution);

		float dpi = System.getProperty("os.name").startsWith("Windows") ? 96f : 72f;
		scale = resolution / dpi;
		System.out.println("scale " + scale);
	}

	private ScaleSupport(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		manualScaleX = scale / scaleX;
		manualScaleY = scale / scaleX;
	}

	/**
	 * コンポーネントのグラフィクス設定からスケールを取得する。
	 * まだ画面に関連付けられていない場合はnullを返す。
	 * @param comp
	 * @return
	 */
	public static ScaleSupport getInstance(Component comp) {
		GraphicsConfiguration gconf = comp.getGraphicsConfiguration();
		if (gconf == null) {
			return null;
		}
		AffineTransform trans = gconf.getDefaultTransform(); // getNormalizingTransform();
		double scaleX = trans.getScaleX();
		double scaleY = trans.getScaleY();
		System.out.println("scale " + scaleX + "," + scaleY);
		return new ScaleSupport(scaleX, scaleY);
	}

	public double getDefaultScaleX() {
		return scaleX;
	}

	public double getDefaultScaleY() {
		return scaleY;
	}

	public double getManualScaleX() {
		return manualScaleX;
	}

	public double getManualScaleY() {
		return manualScaleY;
	}

	public Dimension manualScaled(Dimension dim) {
		return new Dimension((int) (dim.getWidth() * manualScaleX), (int) (dim.getHeight() * manualScaleY));
	}
}