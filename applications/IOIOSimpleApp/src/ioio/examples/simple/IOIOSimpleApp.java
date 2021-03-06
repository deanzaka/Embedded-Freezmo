package ioio.examples.simple;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.NumberPicker;

public class IOIOSimpleApp extends IOIOActivity {
	private TextView textView_;
	private GraphView mGraphView;
	private ToggleButton toggleAuto_;
	private ToggleButton toggleOn_;
	private NumberPicker pickerTemp_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView_ = (TextView) findViewById(R.id.Value);
		toggleAuto_ = (ToggleButton) findViewById(R.id.ToggleAuto);
		toggleOn_ = (ToggleButton) findViewById(R.id.ToggleOn);
		
		mGraphView = (GraphView) findViewById(R.id.graph);
		mGraphView.setMaxValue(100);
		
		pickerTemp_ = (NumberPicker) findViewById(R.id.userTemp);
        pickerTemp_.setMaxValue(100);
        pickerTemp_.setMinValue(0);
        pickerTemp_.setValue(30);
        pickerTemp_.setWrapSelectorWheel(false);
        pickerTemp_.setOnLongPressUpdateInterval(50);

		enableUi(false);
	}

	class Looper extends BaseIOIOLooper {
		private AnalogInput input_;
		private DigitalOutput led_;
		private DigitalOutput fan_;

		@Override
		public void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
			fan_ = ioio_.openDigitalOutput(10);
			input_ = ioio_.openAnalogInput(40);
			enableUi(true);
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			final float reading = input_.read();
			setNumber(reading*350);
			addPoint(reading*350);
			int userTemp = pickerTemp_.getValue();
			if(toggleAuto_.isChecked())	{
				//Automatic control for fan
				if(reading*350 < userTemp) {
				    fan_.write(false);
				    led_.write(true);
				}
				else {
					fan_.write(true);
					led_.write(false);
				}
			}
			else {
				if(toggleOn_.isChecked()) {
					fan_.write(true);
					led_.write(false);
				}
				else {
					fan_.write(false);
					led_.write(true);
				}
			}
			Thread.sleep(200);
		}

		@Override
		public void disconnected() {
			enableUi(false);
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toggleAuto_.setEnabled(enable);
				toggleOn_.setEnabled(enable);
				//pickerTemp_.setEnabled(enable);
			}
		});
	}

	private void setNumber(float f) {
		final String str = String.format("%.2f", f);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(str);
			}
		});
	}
	
	private void addPoint(final float point) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mGraphView.addDataPoint(point);
			}
		});
	}
}