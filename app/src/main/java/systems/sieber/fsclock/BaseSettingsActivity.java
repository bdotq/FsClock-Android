package systems.sieber.fsclock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class BaseSettingsActivity extends AppCompatActivity {

    static final String SHARED_PREF_DOMAIN = "CLOCK";

    static final int PERMISSION_REQUEST = 0;
    static final int PICK_CLOCK_FACE_REQUEST = 1;
    static final int PICK_HOURS_HAND_REQUEST = 2;
    static final int PICK_MINUTES_HAND_REQUEST = 3;
    static final int PICK_SECONDS_HAND_REQUEST = 4;
    static final int PICK_BACKGROUND_REQUEST = 5;

    Gson mGson = new Gson();
    ArrayList<Event> mEvents = new ArrayList<>();
    SharedPreferences mSharedPref;

    LinearLayout mLinearLayoutPurchaseContainer;
    LinearLayout mLinearLayoutSettingsContainer;
    Button mButtonUnlockSettings;
    CheckBox mCheckBoxKeepScreenOn;
    CheckBox mCheckBoxShowBatteryInfo;
    CheckBox mCheckBoxShowBatteryInfoWhenCharging;
    CheckBox mCheckBoxAnalogClockShow;
    CheckBox mCheckBoxAnalogClockShowSeconds;
    Spinner mSpinnerDesignAnalogFace;
    Spinner mSpinnerDesignAnalogHours;
    Spinner mSpinnerDesignAnalogMinutes;
    Spinner mSpinnerDesignAnalogSeconds;
    CheckBox mCheckBoxDigitalClockShow;
    CheckBox mCheckBoxDateShow;
    CheckBox mCheckBoxDigitalClockShowSeconds;
    CheckBox mCheckBoxDigitalClock24Format;
    View mColorChangerAnalogFace;
    View mColorPreviewAnalogFace;
    View mColorChangerAnalogHours;
    View mColorPreviewAnalogHours;
    View mColorChangerAnalogMinutes;
    View mColorPreviewAnalogMinutes;
    View mColorChangerAnalogSeconds;
    View mColorPreviewAnalogSeconds;
    int mColorAnalogFace;
    int mColorAnalogHours;
    int mColorAnalogMinutes;
    int mColorAnalogSeconds;
    boolean mCustomColorAnalogFace;
    boolean mCustomColorAnalogHours;
    boolean mCustomColorAnalogMinutes;
    boolean mCustomColorAnalogSeconds;
    View mColorChangerDigital;
    View mColorPreviewDigital;
    int mColorDigital;
    View mColorChangerBack;
    View mColorPreviewBack;
    int mColorBack;
    CheckBox mCheckBoxBackgroundOwnImage;
    Button mButtonChooseCustomBackground;
    Button mButtonNewEvent;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // init toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // display version & flavor
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            setTitle(getTitle() + " " + pInfo.versionName);
            ((TextView) findViewById(R.id.textViewBuildInfo)).setText(
                    getString(R.string.version) + " " + pInfo.versionName + " (" + pInfo.versionCode + ") " + BuildConfig.BUILD_TYPE + " " + BuildConfig.FLAVOR
            );
        } catch(PackageManager.NameNotFoundException ignored) { }

        // hide dream settings button if not supported
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2
                || uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            findViewById(R.id.buttonDreamSettings).setVisibility(View.GONE);
        }

        // show screensaver note on FireTV
        if(getPackageManager().hasSystemFeature("amazon.hardware.fire_tv")) {
            findViewById(R.id.textViewFireTvNotes).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonDreamSettings).setVisibility(View.GONE);
        }

        // find views
        mLinearLayoutPurchaseContainer = findViewById(R.id.linearLayoutInAppPurchase);
        mLinearLayoutSettingsContainer = findViewById(R.id.linearLayoutSettings);
        mButtonUnlockSettings = findViewById(R.id.buttonUnlockSettings);
        mCheckBoxKeepScreenOn = findViewById(R.id.checkBoxKeepScreenOn);
        mCheckBoxShowBatteryInfo = findViewById(R.id.checkBoxShowBatteryInfo);
        mCheckBoxShowBatteryInfoWhenCharging = findViewById(R.id.checkBoxShowBatteryInfoWhenCharging);
        mCheckBoxAnalogClockShow = findViewById(R.id.checkBoxShowAnalogClock);
        mCheckBoxAnalogClockShowSeconds = findViewById(R.id.checkBoxSecondsAnalog);
        mSpinnerDesignAnalogFace = findViewById(R.id.spinnerDesignAnalogFace);
        mSpinnerDesignAnalogHours = findViewById(R.id.spinnerDesignAnalogHours);
        mSpinnerDesignAnalogMinutes = findViewById(R.id.spinnerDesignAnalogMinutes);
        mSpinnerDesignAnalogSeconds = findViewById(R.id.spinnerDesignAnalogSeconds);
        mCheckBoxDigitalClockShow = findViewById(R.id.checkBoxShowDigitalClock);
        mCheckBoxDateShow = findViewById(R.id.checkBoxShowDate);
        mCheckBoxDigitalClockShowSeconds = findViewById(R.id.checkBoxSecondsDigital);
        mCheckBoxDigitalClock24Format = findViewById(R.id.checkBox24HrsFormat);
        mColorChangerAnalogFace = findViewById(R.id.viewColorChangerAnalogFace);
        mColorPreviewAnalogFace = findViewById(R.id.viewColorPreviewAnalogFace);
        mColorChangerAnalogHours = findViewById(R.id.viewColorChangerAnalogHour);
        mColorPreviewAnalogHours = findViewById(R.id.viewColorPreviewAnalogHour);
        mColorChangerAnalogMinutes = findViewById(R.id.viewColorChangerAnalogMinute);
        mColorPreviewAnalogMinutes = findViewById(R.id.viewColorPreviewAnalogMinute);
        mColorChangerAnalogSeconds = findViewById(R.id.viewColorChangerAnalogSecond);
        mColorPreviewAnalogSeconds = findViewById(R.id.viewColorPreviewAnalogSecond);
        mColorChangerDigital = findViewById(R.id.viewColorChangerDigital);
        mColorPreviewDigital = findViewById(R.id.viewColorPreviewDigital);
        mColorChangerBack = findViewById(R.id.viewColorChangerBack);
        mColorPreviewBack = findViewById(R.id.viewColorPreviewBack);
        mCheckBoxBackgroundOwnImage = findViewById(R.id.checkBoxOwnImageBackground);
        mButtonChooseCustomBackground = findViewById(R.id.buttonChooseBackground);
        mButtonNewEvent = findViewById(R.id.buttonNewEvent);

        // init settings
        mSharedPref = getSharedPreferences(SHARED_PREF_DOMAIN, Context.MODE_PRIVATE);
        mCheckBoxKeepScreenOn.setChecked( mSharedPref.getBoolean("keep-screen-on", true) );
        mCheckBoxShowBatteryInfo.setChecked( mSharedPref.getBoolean("show-battery-info", true) );
        mCheckBoxShowBatteryInfoWhenCharging.setChecked( mSharedPref.getBoolean("show-battery-info-when-charging", false) );
        mCheckBoxAnalogClockShow.setChecked( mSharedPref.getBoolean("show-analog", true) );
        mCheckBoxAnalogClockShowSeconds.setChecked( mSharedPref.getBoolean("show-seconds-analog", true) );
        mCustomColorAnalogFace = mSharedPref.getBoolean("own-color-analog-clock-face", false);
        mCustomColorAnalogHours = mSharedPref.getBoolean("own-color-analog-hours", false);
        mCustomColorAnalogMinutes = mSharedPref.getBoolean("own-color-analog-minutes", false);
        mCustomColorAnalogSeconds = mSharedPref.getBoolean("own-color-analog-seconds", false);
        mCheckBoxDigitalClockShow.setChecked( mSharedPref.getBoolean("show-digital", true) );
        mCheckBoxDateShow.setChecked( mSharedPref.getBoolean("show-date", true) );
        mCheckBoxDigitalClockShowSeconds.setChecked( mSharedPref.getBoolean("show-seconds-digital", true) );
        mCheckBoxDigitalClock24Format.setChecked( mSharedPref.getBoolean("24hrs", true) );
        mColorAnalogFace = mSharedPref.getInt("color-analog-face", 0xffffffff);
        mColorAnalogHours = mSharedPref.getInt("color-analog-hours", 0xffffffff);
        mColorAnalogMinutes = mSharedPref.getInt("color-analog-minutes", 0xffffffff);
        mColorAnalogSeconds = mSharedPref.getInt("color-analog-seconds", 0xffffffff);
        mColorDigital = mSharedPref.getInt("color-digital", 0xffffffff);
        mColorBack = mSharedPref.getInt("color-back", 0xff000000);
        mCheckBoxBackgroundOwnImage.setChecked( mSharedPref.getBoolean("own-image-back", false) );

        // load events
        Event[] eventsArray = mGson.fromJson(mSharedPref.getString("events",""), Event[].class);
        if(eventsArray != null) {
            mEvents = new ArrayList<>(Arrays.asList(eventsArray));
        }
        displayEvents();

        // init UI elements
        initColorPreview();
        initImageSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings_done:
                save();
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case(PICK_CLOCK_FACE_REQUEST):
                if(resultCode == RESULT_OK) {
                    processImage(FILENAME_CLOCK_FACE, data);
                } else {
                    mSpinnerDesignAnalogFace.setSelection(existsImage(FILENAME_CLOCK_FACE) ? 1 : 0, false);
                }
                break;
            case(PICK_HOURS_HAND_REQUEST):
                if(resultCode == RESULT_OK) {
                    processImage(FILENAME_HOURS_HAND, data);
                } else {
                    mSpinnerDesignAnalogHours.setSelection(existsImage(FILENAME_HOURS_HAND) ? 1 : 0, false);
                }
                break;
            case(PICK_MINUTES_HAND_REQUEST):
                if(resultCode == RESULT_OK) {
                    processImage(FILENAME_MINUTES_HAND, data);
                } else {
                    mSpinnerDesignAnalogMinutes.setSelection(existsImage(FILENAME_MINUTES_HAND) ? 1 : 0, false);
                }
                break;
            case(PICK_SECONDS_HAND_REQUEST):
                if(resultCode == RESULT_OK) {
                    processImage(FILENAME_SECONDS_HAND, data);
                } else {
                    mSpinnerDesignAnalogSeconds.setSelection(existsImage(FILENAME_SECONDS_HAND) ? 1 : 0, false);
                }
                break;
            case(PICK_BACKGROUND_REQUEST):
                if(resultCode == RESULT_OK) {
                    processImage(FILENAME_BACKGROUND_IMAGE, data);
                }
                break;
        }
    }

    protected void enableDisableAllSettings(boolean state) {
        mCheckBoxKeepScreenOn.setEnabled(state);
        mCheckBoxShowBatteryInfo.setEnabled(state);
        mCheckBoxShowBatteryInfoWhenCharging.setEnabled(state);
        mCheckBoxAnalogClockShow.setEnabled(state);
        mCheckBoxAnalogClockShowSeconds.setEnabled(state);
        mSpinnerDesignAnalogFace.setEnabled(state);
        mSpinnerDesignAnalogHours.setEnabled(state);
        mSpinnerDesignAnalogMinutes.setEnabled(state);
        mSpinnerDesignAnalogSeconds.setEnabled(state);
        mCheckBoxDigitalClockShow.setEnabled(state);
        mCheckBoxDateShow.setEnabled(state);
        mCheckBoxDigitalClockShowSeconds.setEnabled(state);
        mCheckBoxDigitalClock24Format.setEnabled(state);
        mColorChangerAnalogFace.setEnabled(state);
        mColorChangerAnalogHours.setEnabled(state);
        mColorChangerAnalogMinutes.setEnabled(state);
        mColorChangerAnalogSeconds.setEnabled(state);
        mColorChangerDigital.setEnabled(state);
        mColorChangerBack.setEnabled(state);
        mCheckBoxBackgroundOwnImage.setEnabled(state);
        mButtonChooseCustomBackground.setEnabled(state);
        mButtonNewEvent.setEnabled(state);
    }

    private void initImageSpinner() {
        String[] options = {
                getString(R.string.default_design),
                getString(R.string.custom_image)
        };
        ArrayAdapter dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, options);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerDesignAnalogFace.setAdapter(dataAdapter);
        mSpinnerDesignAnalogFace.setSelection(existsImage(FILENAME_CLOCK_FACE) ? 1 : 0, false);
        mSpinnerDesignAnalogHours.setAdapter(dataAdapter);
        mSpinnerDesignAnalogHours.setSelection(existsImage(FILENAME_HOURS_HAND) ? 1 : 0, false);
        mSpinnerDesignAnalogMinutes.setAdapter(dataAdapter);
        mSpinnerDesignAnalogMinutes.setSelection(existsImage(FILENAME_MINUTES_HAND) ? 1 : 0, false);
        mSpinnerDesignAnalogSeconds.setAdapter(dataAdapter);
        mSpinnerDesignAnalogSeconds.setSelection(existsImage(FILENAME_SECONDS_HAND) ? 1 : 0, false);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getId() == mSpinnerDesignAnalogFace.getId()) {
                    if(i == 0) removeImage(FILENAME_CLOCK_FACE);
                    else chooseImage(PICK_CLOCK_FACE_REQUEST);
                } else if(adapterView.getId() == mSpinnerDesignAnalogHours.getId()) {
                    if(i == 0) removeImage(FILENAME_HOURS_HAND);
                    else chooseImage(PICK_HOURS_HAND_REQUEST);
                } else if(adapterView.getId() == mSpinnerDesignAnalogMinutes.getId()) {
                    if(i == 0) removeImage(FILENAME_MINUTES_HAND);
                    else chooseImage(PICK_MINUTES_HAND_REQUEST);
                } else if(adapterView.getId() == mSpinnerDesignAnalogSeconds.getId()) {
                    if(i == 0) removeImage(FILENAME_SECONDS_HAND);
                    else chooseImage(PICK_SECONDS_HAND_REQUEST);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        };
        mSpinnerDesignAnalogFace.setOnItemSelectedListener(listener);
        mSpinnerDesignAnalogHours.setOnItemSelectedListener(listener);
        mSpinnerDesignAnalogMinutes.setOnItemSelectedListener(listener);
        mSpinnerDesignAnalogSeconds.setOnItemSelectedListener(listener);
    }
    private void initColorPreview() {
        // analog color
        updateColorPreview(mColorAnalogFace, mColorPreviewAnalogFace);
        mColorChangerAnalogFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(mCustomColorAnalogFace, mColorAnalogFace, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        if(applyForAll) {
                            applyColorForAllAnalog(customColor, Color.argb(0xff, red, green, blue));
                        } else {
                            mColorAnalogFace = Color.argb(0xff, red, green, blue);
                            mCustomColorAnalogFace = customColor;
                            updateColorPreview(mColorAnalogFace, mColorPreviewAnalogFace);
                        }
                    }
                });
            }
        });
        updateColorPreview(mColorAnalogHours, mColorPreviewAnalogHours);
        mColorChangerAnalogHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(mCustomColorAnalogHours, mColorAnalogHours, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        if(applyForAll) {
                            applyColorForAllAnalog(customColor, Color.argb(0xff, red, green, blue));
                        } else {
                            mColorAnalogHours = Color.argb(0xff, red, green, blue);
                            mCustomColorAnalogHours = customColor;
                            updateColorPreview(mColorAnalogHours, mColorPreviewAnalogHours);
                        }
                    }
                });
            }
        });
        updateColorPreview(mColorAnalogMinutes, mColorPreviewAnalogMinutes);
        mColorChangerAnalogMinutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(mCustomColorAnalogMinutes, mColorAnalogMinutes, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        if(applyForAll) {
                            applyColorForAllAnalog(customColor, Color.argb(0xff, red, green, blue));
                        } else {
                            mColorAnalogMinutes = Color.argb(0xff, red, green, blue);
                            mCustomColorAnalogMinutes = customColor;
                            updateColorPreview(mColorAnalogMinutes, mColorPreviewAnalogMinutes);
                        }
                    }
                });
            }
        });
        updateColorPreview(mColorAnalogSeconds, mColorPreviewAnalogSeconds);
        mColorChangerAnalogSeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(mCustomColorAnalogSeconds, mColorAnalogSeconds, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        if(applyForAll) {
                            applyColorForAllAnalog(customColor, Color.argb(0xff, red, green, blue));
                        } else {
                            mColorAnalogSeconds = Color.argb(0xff, red, green, blue);
                            mCustomColorAnalogSeconds = customColor;
                            updateColorPreview(mColorAnalogSeconds, mColorPreviewAnalogSeconds);
                        }
                    }
                });
            }
        });

        // digital color
        updateColorPreview(mColorDigital, mColorPreviewDigital);
        mColorChangerDigital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(null, mColorDigital, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        mColorDigital = Color.argb(0xff, red, green, blue);
                        updateColorPreview(mColorDigital, mColorPreviewDigital);
                    }
                });
            }
        });

        // background color
        updateColorPreview(mColorBack, mColorPreviewBack);
        mColorChangerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorDialog(null, mColorBack, new ColorDialogCallback() {
                    @Override
                    public void ok(boolean customColor, int red, int green, int blue, boolean applyForAll) {
                        mColorBack = Color.argb(0xff, red, green, blue);
                        updateColorPreview(mColorBack, mColorPreviewBack);
                    }
                });
            }
        });
    }
    private void applyColorForAllAnalog(boolean customColor, int color) {
        mColorAnalogFace = color;
        mCustomColorAnalogFace = customColor;
        updateColorPreview(mColorAnalogFace, mColorPreviewAnalogFace);
        mColorAnalogHours = color;
        mCustomColorAnalogHours = customColor;
        updateColorPreview(mColorAnalogHours, mColorPreviewAnalogHours);
        mColorAnalogMinutes = color;
        mCustomColorAnalogMinutes = customColor;
        updateColorPreview(mColorAnalogMinutes, mColorPreviewAnalogMinutes);
        mColorAnalogSeconds = color;
        mCustomColorAnalogSeconds = customColor;
        updateColorPreview(mColorAnalogSeconds, mColorPreviewAnalogSeconds);
    }
    private void updateColorPreview(int color, View v) {
        v.setBackgroundColor(Color.argb(0xff, Color.red(color), Color.green(color), Color.blue(color)));
    }
    interface ColorDialogCallback {
        void ok(boolean customColor, int red, int green, int blue, boolean applyForAll);
    }
    private void showColorDialog(Boolean customColor, int initialColor, final ColorDialogCallback colorDialogFinished) {
        final Dialog ad = new Dialog(this);
        ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ad.setContentView(R.layout.dialog_color);
        final CheckBox checkBoxCustomColor = ad.findViewById(R.id.checkBoxCustomColor);
        final SeekBar seekBarRed = ad.findViewById(R.id.seekBarRed);
        final SeekBar seekBarGreen = ad.findViewById(R.id.seekBarGreen);
        final SeekBar seekBarBlue = ad.findViewById(R.id.seekBarBlue);
        final View colorPreview = ad.findViewById(R.id.viewColorPreview);
        final Button buttonOkForAll = ad.findViewById(R.id.buttonOkForAll);
        if(customColor == null) {
            checkBoxCustomColor.setVisibility(View.GONE);
            buttonOkForAll.setVisibility(View.GONE);
        } else {
            checkBoxCustomColor.setChecked(customColor);
            if(!customColor) {
                seekBarRed.setEnabled(false);
                seekBarGreen.setEnabled(false);
                seekBarBlue.setEnabled(false);
            }
        }
        checkBoxCustomColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                seekBarRed.setEnabled(b);
                seekBarGreen.setEnabled(b);
                seekBarBlue.setEnabled(b);
            }
        });
        seekBarRed.setProgress(Color.red(initialColor));
        seekBarGreen.setProgress(Color.green(initialColor));
        seekBarBlue.setProgress(Color.blue(initialColor));
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColorPreview(Color.argb(0xff, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress()), colorPreview);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColorPreview(Color.argb(0xff, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress()), colorPreview);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColorPreview(Color.argb(0xff, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress()), colorPreview);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        updateColorPreview(Color.argb(0xff, seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress()), colorPreview);
        ad.show();
        ad.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ad.findViewById(R.id.buttonOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorDialogFinished != null) {
                    colorDialogFinished.ok(checkBoxCustomColor.isChecked(), seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress(), false);
                }
                ad.dismiss();
            }
        });
        ad.findViewById(R.id.buttonOkForAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorDialogFinished != null) {
                    colorDialogFinished.ok(checkBoxCustomColor.isChecked(), seekBarRed.getProgress(), seekBarGreen.getProgress(), seekBarBlue.getProgress(), true);
                }
                ad.dismiss();
            }
        });
    }

    private void save() {
        SharedPreferences.Editor editor = mSharedPref.edit();

        editor.putBoolean("keep-screen-on", mCheckBoxKeepScreenOn.isChecked());
        editor.putBoolean("show-battery-info", mCheckBoxShowBatteryInfo.isChecked());
        editor.putBoolean("show-battery-info-when-charging", mCheckBoxShowBatteryInfoWhenCharging.isChecked());
        editor.putBoolean("show-analog", mCheckBoxAnalogClockShow.isChecked());
        editor.putBoolean("own-color-analog-clock-face", mCustomColorAnalogFace);
        editor.putBoolean("own-color-analog-hours", mCustomColorAnalogHours);
        editor.putBoolean("own-color-analog-minutes", mCustomColorAnalogMinutes);
        editor.putBoolean("own-color-analog-seconds", mCustomColorAnalogSeconds);
        editor.putBoolean("show-digital", mCheckBoxDigitalClockShow.isChecked());
        editor.putBoolean("show-date", mCheckBoxDateShow.isChecked());
        editor.putBoolean("show-seconds-analog", mCheckBoxAnalogClockShowSeconds.isChecked());
        editor.putBoolean("show-seconds-digital", mCheckBoxDigitalClockShowSeconds.isChecked());
        editor.putBoolean("24hrs", mCheckBoxDigitalClock24Format.isChecked());
        editor.putString("events", mGson.toJson(mEvents.toArray()));
        editor.putInt("color-analog-face", mColorAnalogFace);
        editor.putInt("color-analog-hours", mColorAnalogHours);
        editor.putInt("color-analog-minutes", mColorAnalogMinutes);
        editor.putInt("color-analog-seconds", mColorAnalogSeconds);
        editor.putInt("color-digital", mColorDigital);
        editor.putInt("color-back", mColorBack);
        editor.putBoolean("own-image-back", mCheckBoxBackgroundOwnImage.isChecked());

        editor.apply();
    }

    private void chooseImage(int requestId) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestId);
        Toast.makeText(this, getString(R.string.own_images_note), Toast.LENGTH_LONG).show();
    }
    public void onClickChooseBackground(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_BACKGROUND_REQUEST);
    }

    private void displayEvents() {
        LinearLayout llEvents = findViewById(R.id.linearLayoutSettingsEvents);
        llEvents.removeAllViews();
        for(final Event e : mEvents) {
            Button b = new Button(this);
            b.setText(e.toString());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickEditEvent(e);
                }
            });
            llEvents.addView(b);
        }
    }
    public void onClickNewEvent(View v) {
        final Dialog ad = new Dialog(this);
        ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ad.setContentView(R.layout.dialog_event);
        //ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        ((NumberPicker) ad.findViewById(R.id.numberPickerHour)).setMaxValue(23);
        ((NumberPicker) ad.findViewById(R.id.numberPickerMinute)).setMaxValue(59);
        ad.findViewById(R.id.buttonNewEventRemove).setVisibility(View.GONE);
        ad.show();
        ad.findViewById(R.id.buttonNewEventOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEvents.add(new Event(
                        ((NumberPicker) ad.findViewById(R.id.numberPickerHour)).getValue(),
                        ((NumberPicker) ad.findViewById(R.id.numberPickerMinute)).getValue(),
                        ((EditText) ad.findViewById(R.id.editTextTitleNewEvent)).getText().toString(),
                        ((EditText) ad.findViewById(R.id.editTextSpeakNewEvent)).getText().toString(),
                        ((CheckBox) ad.findViewById(R.id.checkBoxAlarmNewEvent)).isChecked(),
                        ((CheckBox) ad.findViewById(R.id.checkBoxDisplayNewEvent)).isChecked()
                ));
                ad.dismiss();
                displayEvents();
            }
        });
    }
    public void onClickEditEvent(final Event e) {
        final Dialog ad = new Dialog(this);
        ad.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ad.setContentView(R.layout.dialog_event);
        //ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        ((NumberPicker) ad.findViewById(R.id.numberPickerHour)).setMaxValue(23);
        ((NumberPicker) ad.findViewById(R.id.numberPickerMinute)).setMaxValue(59);
        ((NumberPicker) ad.findViewById(R.id.numberPickerHour)).setValue(e.triggerHour);
        ((NumberPicker) ad.findViewById(R.id.numberPickerMinute)).setValue(e.triggerMinute);
        ((EditText) ad.findViewById(R.id.editTextTitleNewEvent)).setText(e.title);
        ((EditText) ad.findViewById(R.id.editTextSpeakNewEvent)).setText(e.speakText);
        ((CheckBox) ad.findViewById(R.id.checkBoxAlarmNewEvent)).setChecked(e.playAlarm);
        ((CheckBox) ad.findViewById(R.id.checkBoxDisplayNewEvent)).setChecked(e.showOnScreen);
        ad.show();
        ad.findViewById(R.id.buttonNewEventOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e.triggerHour = ((NumberPicker) ad.findViewById(R.id.numberPickerHour)).getValue();
                e.triggerMinute = ((NumberPicker) ad.findViewById(R.id.numberPickerMinute)).getValue();
                e.title = ((EditText) ad.findViewById(R.id.editTextTitleNewEvent)).getText().toString();
                e.speakText = ((EditText) ad.findViewById(R.id.editTextSpeakNewEvent)).getText().toString();
                e.playAlarm = ((CheckBox) ad.findViewById(R.id.checkBoxAlarmNewEvent)).isChecked();
                e.showOnScreen = ((CheckBox) ad.findViewById(R.id.checkBoxDisplayNewEvent)).isChecked();
                ad.dismiss();
                displayEvents();
            }
        });
        ad.findViewById(R.id.buttonNewEventRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEvents.remove(e);
                ad.dismiss();
                displayEvents();
            }
        });
    }

    private void processImage(String path, Intent data) {
        if(data == null || data.getData() == null) return;
        try {
            File fl = getStorage(this, path);
            InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
            byte[] targetArray = new byte[inputStream.available()];
            inputStream.read(targetArray);
            FileOutputStream stream = new FileOutputStream(fl);
            stream.write(targetArray);
            stream.flush();
            stream.close();
            scanFile(fl);
        } catch(Exception ignored) { }
    }
    private void removeImage(String path) {
        try {
            File fl = getStorage(this, path);
            fl.delete();
            scanFile(fl);
        } catch(Exception ignored) { }
    }
    private boolean existsImage(String path) {
        try {
            File fl = getStorage(this, path);
            return fl.exists();
        } catch(Exception ignored) { }
        return false;
    }

    private void scanFile(File f) {
        Uri uri = Uri.fromFile(f);
        Intent scanFileIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    public static String FILENAME_CLOCK_FACE = "clockface.png";
    public static String FILENAME_HOURS_HAND = "hour.png";
    public static String FILENAME_MINUTES_HAND = "minute.png";
    public static String FILENAME_SECONDS_HAND = "second.png";
    public static String FILENAME_BACKGROUND_IMAGE = "bg.png";

    public static File getStorage(Context c, String filename) {
        File exportDir = c.getExternalFilesDir(null);
        return new File(exportDir, filename);
    }

    public void onClickDreamSettings(View v) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS));
            } catch(ActivityNotFoundException e) {
                infoDialog(null, getString(R.string.screensaver_not_supported));
            }
        }
    }

    public final static String APPID_CUSTOMERDB    = "de.georgsieber.customerdb";
    public final static String APPID_REMOTEPOINTER = "systems.sieber.remotespotlight";
    public final static String APPID_BALLBREAK     = "de.georgsieber.ballbreak";
    public final static String URL_OCO             = "https://github.com/schorschii/oco-server";
    public final static String URL_MASTERPLAN      = "https://github.com/schorschii/masterplan";

    public void onClickGithub(View v) {
        openBrowser(getString(R.string.project_website));
    }
    public void onClickCustomerDatabaseApp(View v) {
        openPlayStore(APPID_CUSTOMERDB);
    }
    public void onClickRemotePointerApp(View v) {
        openPlayStore(APPID_REMOTEPOINTER);
    }
    public void onClickBallBreakApp(View v) {
        openPlayStore(APPID_BALLBREAK);
    }
    public void onClickOco(View v) {
        openBrowser(URL_OCO);
    }
    public void onClickMasterplan(View v) {
        openBrowser(URL_MASTERPLAN);
    }
    void openBrowser(String url) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch(SecurityException ignored) {
            infoDialog(getString(R.string.no_web_browser_found), url);
        } catch(ActivityNotFoundException ignored) {
            infoDialog(getString(R.string.no_web_browser_found), url);
        }
    }
    private void openPlayStore(String appId) {
        try {
            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId)));
        } catch(android.content.ActivityNotFoundException anfe) {
            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId)));
        }
    }

    public void onClickAllowSystemCalendarAccess(View v) {
        int permissionCheckResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);
        if(permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            infoDialog(null, getString(R.string.access_already_granted));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST);
        }
    }

    private void infoDialog(String title, String text) {
        final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        if(title != null) dlg.setTitle(title);
        if(text != null) dlg.setMessage(text);
        dlg.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dlg.setCancelable(true);
        dlg.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST) {
            for(int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        boolean showRationale = shouldShowRequestPermissionRationale(permission);
                        if(!showRationale) {
                            // user also CHECKED "never ask again"
                            infoDialog(null, getString(R.string.access_denied_by_user));
                        }
                    }
                }
            }
        }
    }

}
