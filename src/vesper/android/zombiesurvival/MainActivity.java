package vesper.android.zombiesurvival;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button mStart;
	
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	startActivity(new Intent(this, TestActivity.class));
    	return true;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mStart = (Button)findViewById(R.id.btn_StartTestActivity);
        mStart.setOnClickListener(
        		new OnClickListener() {
					public void onClick(View v) {
						startTest();
					}
				});
    }
	
	public void startTest() {
		startActivity(new Intent(this, TestActivity.class));
		//Toast.makeText(this, "Its a lie", Toast.LENGTH_LONG).show();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
