package scikit.util;

import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public interface Window {
	public String getTitle();
	public JComponent getComponent();
	public BufferedImage getImage(int width, int height);
	public void clear();
	public void animate();
}
