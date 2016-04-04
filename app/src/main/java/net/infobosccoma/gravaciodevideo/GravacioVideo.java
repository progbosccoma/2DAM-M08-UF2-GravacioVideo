package net.infobosccoma.gravaciodevideo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

/*
 * Tipus enumerat
 */
enum Estat {
	GRAVANT, ATURAT
};

/**
 * Classe principal de l'aplicació
 * 
 * @author Marc Nicolau
 *
 */

public class GravacioVideo extends Activity implements OnClickListener {
	// Atributs de la classe
	private Button btnGravar;
	private MediaRecorder gravador;
	private Camera camera;
	private PrevisualitzacioCamera prev;
	private Estat estatGravacio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gravacio_video);

		btnGravar = (Button) findViewById(R.id.btnGravar);
		btnGravar.setOnClickListener(this);

		// Comprovar si el dispositiu té càmera
		if (teCamera()) {
			// obtenir l'objecte càmera
			camera = getCamera();
			if (camera != null) {
				// indicar amb quina orientació s'ha de visualitzar la càmera del darrera
				setCameraDisplayOrientation(this,
						Camera.CameraInfo.CAMERA_FACING_BACK, camera);
				// obtenir l'objecte per previsualitzar la càmera
				prev = new PrevisualitzacioCamera(this, camera);
				FrameLayout frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
				frameLayout.addView(prev);
			}
		} else {
			Dialeg.newInstance(getApplicationInfo().name, "El dispositiu no té càmera").show(getFragmentManager(),"Alert");
		}

		estatGravacio = Estat.ATURAT;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gravacio_video, menu);
		return true;
	}

	/*
	 * Comprova si el dispositiu té càmera
	 */
	private boolean teCamera() {
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * Obtenir i obrir l'objecte Camera
	 */
	public Camera getCamera() {
		Camera c = null;

		try {
			c = Camera.open();
		} catch (Exception e) {
            Dialeg.newInstance("Error càmera", e.getMessage()).show(getFragmentManager(),"Alert");
		}
		return c;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnGravar) {
			if (estatGravacio == Estat.ATURAT) {
				prepararGravacio();
				gravador.start();
				btnGravar.setBackgroundColor(getResources().getColor(
						R.color.vermell));
				btnGravar.setText(R.string.btnAturarGravacio);
				estatGravacio = Estat.GRAVANT;
			} else {
				aturarGravacio();
				btnGravar.setBackgroundColor(getResources().getColor(
						R.color.verd));
				btnGravar.setText(R.string.btnIniciarGravacio);
				estatGravacio = Estat.ATURAT;
			}
		}
	}

	/*
	 * Obtenir l'orientació de la càmera en funció de l'orientació de la
	 * pantalla
	 */
	private static int getCameraOrientation(Activity activity, int idCamera) {
		int result;

		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(0, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensar l'efecte mirall
		} else { // càmera de darrera
			result = (info.orientation - degrees + 360) % 360;
		}

		return result;
	}

	/*
	 * Assignar l'orientació de la càmera (previsualització)
	 */
	public static void setCameraDisplayOrientation(Activity activity,
			int idCamera, android.hardware.Camera camera) {
		camera.setDisplayOrientation(getCameraOrientation(activity,
				Camera.CameraInfo.CAMERA_FACING_BACK));
	}

	/*
	 * Configurar els paràmetres del gravador
	 */
	private void prepararGravacio() {
		if (gravador != null) {
			aturarGravacio();
		}
		try {
			camera.unlock();
			gravador = new MediaRecorder();
			// quina càmera utilitza el gravador
			gravador.setCamera(camera);
			// orígen del vídeo i de l'àudio
			gravador.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			gravador.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			// formats de sortida
			gravador.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			// codificació del vídeo i de l'àudio
			gravador.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
			gravador.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			// establir el nom del fitxer de sortida
			gravador.setOutputFile(getNomFitxer().getAbsolutePath());
			// assignar la previsualització
			gravador.setPreviewDisplay(prev.getHolder().getSurface());
			// indicar amb quina orientació s'ha de gravar
			gravador.setOrientationHint(getCameraOrientation(this,
					Camera.CameraInfo.CAMERA_FACING_BACK));
			// tot preparat!
			gravador.prepare();
		} catch (Exception e) {
			Dialeg.newInstance("Gravació",String.format("ERROR en iniciar la gravació: %s",
						e.getMessage())).show(getFragmentManager(), "Alert");
			aturarGravacio();
		}
	}

	/*
	 * Establir el nom del fitxer on es desarà el vídeo
	 */
	private File getNomFitxer() {
		// Crear el nom del fitxer del vídeo
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "video_" + timeStamp + ".3gp";
		File path = new File(Environment.getExternalStorageDirectory(),
				this.getPackageName());
		if (!path.exists())
			path.mkdirs();

		return new File(path, imageFileName);
	}

	private void aturarGravacio() {
		if (gravador != null) {
			gravador.reset(); // esborrar la configuració del gravador
			gravador.release(); // alliberar els recursos de l'objecte
			gravador = null;
			camera.lock(); // bloquejar la camera
		}
	}

	private void alliberarCamera() {
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		aturarGravacio();
		alliberarCamera();
	}
}
