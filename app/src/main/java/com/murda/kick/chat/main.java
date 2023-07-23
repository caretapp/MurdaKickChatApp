package com.murda.kick.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class main extends AppCompatActivity {
	private Context context;
	public static WeakReference<Activity> instance = new WeakReference<>(null);
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;

	private final ActivityResultLauncher<Intent> drawOverPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
				if (Settings.canDrawOverlays(this)) {
					Echo(getLocalClassName(), "Nice!", 1);
					startService(new Intent(context, widget.class));
				}
			});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = getApplicationContext();
		instance = new WeakReference<>(this);
		prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

		findViewById(R.id.go_btn).setOnClickListener(v->{
			if ( ! Settings.canDrawOverlays(this)) {
				drawOverPermissionLauncher.launch( new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION) );
			} else {
				if ( ! widget.isRunning ) {
					startService(new Intent(context, widget.class));
				} else {
					stopService(new Intent(context, widget.class));
				}
			}
		});
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		SeekBar widthSeeker = findViewById(R.id.seekBar_width);
		widthSeeker.setMax(displayMetrics.widthPixels);
		widthSeeker.setProgress(prefs.getInt("widget_width", 400));
		TextView seekerWTxt = findViewById(R.id.textView_width);
		seekerWTxt.setText(String.valueOf( prefs.getInt("widget_width", 400) ));
		widthSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				prefsEditor = prefs.edit();
				prefsEditor.putInt( "widget_width", progress ).apply();
				seekerWTxt.setText(String.valueOf( progress ));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if ( widget.isRunning ) {
					stopService(new Intent(context, widget.class));
					if ( ! Settings.canDrawOverlays(context)) {
						startService(new Intent(context, widget.class));
					}
				}
			}
		});
		SeekBar heightSeeker = findViewById(R.id.seekBar_height);
		heightSeeker.setMax(displayMetrics.heightPixels);
		heightSeeker.setProgress(prefs.getInt("widget_height", 800) );
		TextView seekerHTxt = findViewById(R.id.textView_height);
		seekerHTxt.setText(String.valueOf( prefs.getInt("widget_height", 800) ));
		heightSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				prefsEditor = prefs.edit();
				prefsEditor.putInt( "widget_height", progress ).apply();
				seekerHTxt.setText(String.valueOf( progress ));
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if ( widget.isRunning ) {
					stopService(new Intent(context, widget.class));
					if ( ! Settings.canDrawOverlays(context)) {
						startService(new Intent(context, widget.class));
					}
				}
			}
		});
		SeekBar alphaSeeker = findViewById(R.id.seekBar_alpha);
		alphaSeeker.setProgress( Math.round(prefs.getFloat("widget_alpha", 1) * 100 ) );
		TextView seekerATxt = findViewById(R.id.textView_alpha);
		seekerATxt.setText( String.valueOf( Math.round(prefs.getFloat("widget_alpha", 1) * 100 )) );
		alphaSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				prefsEditor = prefs.edit();
				float floatVal = progress / 100f;
				if ( progress % 10 == 0 ) {
					Log.i("fatfart", floatVal + "");
					prefsEditor.putFloat("widget_alpha", floatVal).apply();
					seekerATxt.setText(String.valueOf(progress));
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if ( widget.isRunning ) {
					stopService(new Intent(context, widget.class));
					if ( ! Settings.canDrawOverlays(context)) {
						startService(new Intent(context, widget.class));
					}
				}
			}
		});
	}

	public static void Echo(String c, String m, int level ) {
		switch (level) {
			case 1:
				Log.i(c, m);
				break;
			case 2:
				Log.d(c, m);
				break;
			default:
				Log.e(c, m);
				break;
		}
	}
}