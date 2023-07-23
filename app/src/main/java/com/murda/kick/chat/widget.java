package com.murda.kick.chat;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;


public class widget extends Service {
	private Context context;
	public static boolean isRunning;
	private SharedPreferences prefs;
	private int lastHeight = 50;
	private int lastWidth = 50;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams params;
	private ViewGroup mainView;
	private View expandedView;
	private View collapsedView;
	private final DisplayMetrics displayMetrics = new DisplayMetrics();

	private View.OnTouchListener getMainTouchListener() {
		return new View.OnTouchListener() {
			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						//remember the initial position.
						initialX = params.x;
						initialY = params.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						return true;
					case MotionEvent.ACTION_UP:
						int diff = (int) (event.getRawY() - initialTouchY);
						if (diff > -3 && diff < 3 ) {
							showHide();
						}
						return true;
					case MotionEvent.ACTION_MOVE:
						lastWidth = initialX + (int) (event.getRawX() - initialTouchX);
						lastHeight = initialY + (int) (event.getRawY() - initialTouchY);
						params.x = lastWidth;
						params.y = lastHeight;
						mWindowManager.updateViewLayout(mainView, params);
						return true;
				}
				return false;
			}
		};
	}

	public widget() {

	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void showHide(){
		if ( collapsedView.getVisibility() == View.VISIBLE ) {
			collapsedView.setVisibility(View.GONE);
			expandedView.setVisibility(View.VISIBLE);
		} else {
			collapsedView.setVisibility(View.VISIBLE);
			expandedView.setVisibility(View.GONE);
		}
		mainView.postDelayed(() -> {
			main.instance.get().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			int[] pos = new int[2];
			mainView.getLocationOnScreen( pos );
			if ( pos[0] < 50 ) {
				params.x = 50;
				params.y = 50;
				mWindowManager.updateViewLayout(mainView, params);
				vibrate(context,500);
			} else if ( pos[0] > displayMetrics.widthPixels ) {
				params.x = 50;
				params.y = 50;
				mWindowManager.updateViewLayout(mainView, params);
				vibrate(context,500);
			}
			if ( pos[1] < 50 ) {
				params.x = 50;
				params.y = 50;
				mWindowManager.updateViewLayout(mainView, params);
				vibrate(context,500);
			} else if ( pos[1] > displayMetrics.heightPixels ) {
				params.x = 50;
				params.y = 50;
				mWindowManager.updateViewLayout(mainView, params);
				vibrate(context,500);
			}
		}, 1000);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		isRunning = true;
		main.instance.get().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

		mainView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.widget, null);
		params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.TOP | Gravity.START;
		params.y = lastHeight;
		params.x = lastWidth;
		mainView.setClipChildren(false);
		mainView.setAlpha( prefs.getFloat("widget_alpha", 1) );
		//mainView.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_item_1));
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mWindowManager.addView(mainView, params);

		LinearLayout webViewHolder = mainView.findViewById(R.id.webview_holder);
		WebView webView = new WebView( context );
		webView.setWebChromeClient(new chatChromeClient());
		webView.setWebViewClient(new chatView());
		//webView.clearCache(true);
		//webView.clearHistory();
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.setLayoutParams( new LinearLayout.LayoutParams(
				prefs.getInt("widget_width", 400),
				prefs.getInt("widget_height", 800)) );
		webViewHolder.addView( webView );
		webView.loadUrl( "https://kick.com/murda/chatroom" );

		expandedView = mainView.findViewById(R.id.widget_exp_view);
		collapsedView = mainView.findViewById(R.id.widget_min_view);
		mainView = mainView.findViewById(R.id.widget_main);
		mainView.setOnTouchListener(getMainTouchListener());

		mainView.findViewById(R.id.widget_back).setOnClickListener(v -> {
			if (webView.canGoBack()) {
				webView.goBack();
				vibrate(context,100);
			} else {
				vibrate(context,500);
			}
		});

		mainView.findViewById(R.id.widget_forward).setOnClickListener(v -> {
			if (webView.canGoForward()) {
				webView.goForward();
				vibrate(context,100);
			} else {
				vibrate(context,500);
			}
		});

		mainView.findViewById(R.id.widget_refresh).setOnClickListener(v -> {
			webView.reload();
			vibrate(context,100);
		});

		mainView.findViewById(R.id.widget_close).setOnClickListener(v -> {
			vibrate(context,100);
			stopSelf();
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mainView != null) {
			mWindowManager.removeView(mainView);
			isRunning = false;
		}
	}

	public void vibrate (Context c, int dur) {
		Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(VibrationEffect.createOneShot(dur, VibrationEffect.DEFAULT_AMPLITUDE));
	}
}
