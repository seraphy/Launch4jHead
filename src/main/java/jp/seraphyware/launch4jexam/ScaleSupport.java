package jp.seraphyware.launch4jexam;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * スクリーンのスケールを取得する。
 *
 * Java8であれば自動スケールがかかっていないので、マニュアルでスケール倍して座標を補正する。
 * Java11であれば自動スケールがかかっている。
 */
public class ScaleSupport {

	private static final int resolution;

	private static final float computeScale;

	private static final boolean noNeedCheckScaleByReflection;

	private double scaleX;

	private double scaleY;

	private double manualScaleX;

	private double manualScaleY;

	private boolean retina;

	static {
		// デフォルトのスクリーン解像度を取得する
		Toolkit tk = Toolkit.getDefaultToolkit();
		resolution = tk.getScreenResolution();

		// スクリーン解像度と標準のDPIから必要とされるスケールを計算する。
		// ただし、Windowsでない場合はスケール1の等倍にする。
		// (Retinaの場合はシステム側でスケールされるのでアプリ側でスケールする必要はないため)
		float dpi = System.getProperty("os.name").startsWith("Windows") ? 96f : resolution;
		computeScale = resolution / dpi;

		// Java9以降であればアフィン変換パラメータでスケールを確認できるので
		// リフレクションを使ったスケールの確認は不要である。
		noNeedCheckScaleByReflection = JavaVersionUtils.getJavaVersion() >= 9;
	}

	private ScaleSupport(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		if (scaleX > 1 || scaleY > 1 || Boolean.getBoolean("disableScaleCalibrate")) {
			// システム側でスケールがかかっていれば、アプリ側ではスケールする必要はない。
			// もしくはシステムプロパティでアプリによるスケールを無効にしている場合。
			manualScaleX = 1;
			manualScaleY = 1;
		} else {
			// システム側でスケールがかかっていない場合はアプリ側でスケールする
			// スクリーン解像度とDPIから必要なスケールを求める
			manualScaleX = computeScale;
			manualScaleY = computeScale;
		}
	}

	public static int getScreenResolution() {
		return resolution;
	}

	public static float getScreenScale() {
		return computeScale;
	}

	private void setRetina(boolean retina) {
		this.retina = retina;
	}

	public boolean isRetina() {
		return retina;
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
		return getInstance(gconf);
	}

	public static ScaleSupport getDefault() {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gdev = genv.getDefaultScreenDevice();
		return getInstance(gdev.getDefaultConfiguration());
	}

	public static ScaleSupport getInstance(GraphicsConfiguration gconf) {
		// java9以降であれば、GraphicsConfigurationのデフォルトのアフィン変換に
		// スクリーンのスケールがかけられている。
		AffineTransform trans = gconf.getDefaultTransform();
		double scaleX = trans.getScaleX();
		double scaleY = trans.getScaleY();

		boolean retina = false;
		if (scaleX == 1 && scaleY == 1 && !noNeedCheckScaleByReflection) {
			// Java8まではデフォルトのアフィン変換はスクリーンスケールは設定されていないので
			// 等倍を返してきた場合は、グラフィクスデバイスがスケールメソッドをもっているかリフレクションで確かめる。
			// Mac版のJava8であればスケーメメソッドをもっている。
			// (Java9以降であれば確認は不要である。)
			// http://hg.openjdk.java.net/jdk9/client/jdk/file/1089d8a8a6e1/src/java.desktop/macosx/classes/sun/awt/CGraphicsDevice.java
			// https://www.programcreek.com/java-api-examples/?code=SensorsINI/jaer/jaer-master/src/net/sf/jaer/graphics/ChipCanvas.java
			final GraphicsDevice device = gconf.getDevice();
			Object scaleObj = null;
			try {
				// public methodがあれば、そちらを試す。
				Method methodGetScaleFactor = device.getClass().getMethod("getScaleFactor");
				scaleObj = methodGetScaleFactor.invoke(device);

			} catch (Exception ex) {
				try {
					// sun.awt.CGraphicsDevice固有の内部フィールドを試す
					Field field = device.getClass().getDeclaredField("scale");
					field.setAccessible(true);
					scaleObj = field.get(device);
				} catch (Exception ex2) {
					// 何もしない
				}
			}
			if (scaleObj instanceof Number) {
				int scale = ((Number) scaleObj).intValue();
				scaleX = scaleY = scale;
				if (scale >= 2) {
					retina = true;
				}
			}
		}
		ScaleSupport inst = new ScaleSupport(scaleX, scaleY);
		inst.setRetina(retina);
		return inst;
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