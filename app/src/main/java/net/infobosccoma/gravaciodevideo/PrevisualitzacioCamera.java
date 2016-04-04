package net.infobosccoma.gravaciodevideo;

import java.io.IOException;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Classe que hereta de SurfaceView per mostrar el contingut de la càmera
 * 
 * @author Marc Nicolau
 *
 */
public class PrevisualitzacioCamera extends SurfaceView implements
		SurfaceHolder.Callback {

	// objecte que mostra la previsualització
	private SurfaceHolder holder;
	// referència a la càmera
	private Camera camera;
	// referència al context de l'aplicació
	private Context context;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param camera
	 */
	public PrevisualitzacioCamera(Context context, Camera camera) {
		super(context);
		this.context = context;
		this.camera = camera;
		// configurar el SurfaceHolder
		holder = getHolder();
		holder.addCallback(this);
		// està deprecated, però cal en versions anteriors a Android 3.0
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if (holder.getSurface() == null) {
			// la zona de previsualització no existeix
			throw new UnsupportedOperationException("No hi ha cap zona de previsualització disponible");
		}

		// aturar la previsualització abans de fer canvis
		try {
			camera.stopPreview();
		} catch (Exception e) {

		}

		// establir la mida de previsualitzacó i redimensionar o rotar
		// iniciar la previsualització amb els nous canvis
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception e) {
			throw new UnsupportedOperationException(String.format("Error iniciant la previsualització de la càmera : %s", e.getMessage()));
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// connectar la càmera amb la vista
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			throw new UnsupportedOperationException("Error mentre es configurava la previsualització");

		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.release();
	}

}
