package com.biniisu.leanrss.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.biniisu.leanrss.R;
import com.biniisu.leanrss.persistence.preferences.ReadablyPrefs;
import com.biniisu.leanrss.persistence.preferences.ReadingPrefs;
import com.biniisu.leanrss.ui.controllers.FontChooserAdapter;

/**
 * Created by biniam_Haddish on 8/29/17.
 * <p>
 * A popup window to let the user choose fonts, background color, font size, line height and justification.
 * It also declares {@link ReadingAppearanceCallback} for interested components to implement.
 */

public class ReadingAppearanceSettings extends PopupWindow implements FontChooserAdapter.FontSelectedCallback {


    public static final int FONT_SIZE_MAX_LIMIT = 35;
    public static final int FONT_SIZE_MIN_LIMIT = 13;
    public static final float LINE_HEIGHT_MAX_LIMIT = 2.0f;
    public static final float LINE_HEIGHT_MIN_LIMIT = 1.0f;
    private static final String JUSTIFY_ALIGNMENT = "justify";
    private static final String LEFT_ALIGNMENT = "left";
    private ReadingAppearanceCallback readingAppearanceCallback;
    private Context context;

    // Views
    private RelativeLayout container;
    private ImageButton justifyText;
    private ImageButton alignLeftText;
    private ImageButton decreaseFontSize;
    private ImageButton increaseFontSize;
    private ImageButton decreaseLineHeight;
    private ImageButton increaseLineHeight;
    private TextView currentFontSize;
    private TextView currentLineHeight;
    private FontChooserAdapter fontChooserAdapter;
    private ReadablyPrefs readablyPrefs;
    private LinearLayout backgroundColorContainer;
    private Switch autoDarkModeSwitch;
    private TextView autoDarkModeDescriptionTextView;
    private ImageView openReadingPrefsImageView;
    private ReadingPrefs readingPrefs;

    public ReadingAppearanceSettings(Context context) {
        super(context);
        this.context = context;

        readablyPrefs = ReadablyPrefs.getInstance(context);
        readingPrefs = ReadingPrefs.getInstance(context);
        View popupView = LayoutInflater.from(context).inflate(R.layout.reading_appearance_layout, null);

        container = popupView.findViewById(R.id.appearanceSettings);
        justifyText = popupView.findViewById(R.id.justifyText);
        alignLeftText = popupView.findViewById(R.id.alingLeftText);
        increaseFontSize = popupView.findViewById(R.id.increaseFontSize);
        decreaseFontSize = popupView.findViewById(R.id.decreaseFontSize);
        decreaseLineHeight = popupView.findViewById(R.id.decreaseLineHeight);
        increaseLineHeight = popupView.findViewById(R.id.increaseLineHeight);
        currentFontSize = popupView.findViewById(R.id.currentFontSize);
        currentLineHeight = popupView.findViewById(R.id.currentLineHeight);
        backgroundColorContainer = popupView.findViewById(R.id.backgroundColorContainer);
        autoDarkModeSwitch = popupView.findViewById(R.id.autoDarkModeSwitch);
        autoDarkModeDescriptionTextView = popupView.findViewById(R.id.autoDarkModeDescription);
        openReadingPrefsImageView = popupView.findViewById(R.id.openReadingPrefs);

        openReadingPrefsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SettingsSubCategoryActivity.class);
                intent.putExtra(SettingsSubCategoryActivity.PREF_CAT_EXTRA, 0);
                context.startActivity(intent);
            }
        });

        autoDarkModeSwitch.setOnClickListener(view -> {
            if (readingAppearanceCallback != null) {
                readingAppearanceCallback.onAutoDarkModeSwitchClicked(autoDarkModeSwitch.isChecked());
            }

            if (autoDarkModeSwitch.isChecked()) {
                backgroundColorContainer.setVisibility(View.GONE);
            } else {
                backgroundColorContainer.setVisibility(View.VISIBLE);
            }
        });

        autoDarkModeSwitch.setChecked(readablyPrefs.autoDarkMode);

        setContentView(popupView);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(context.getResources().getDimensionPixelSize(R.dimen.font_chooser_width));
        setBackgroundDrawable(context.getDrawable(R.drawable.transparent_bg_drawable));


        setOutsideTouchable(true);
        setElevation(8f);
        setInputMethodMode(INPUT_METHOD_NOT_NEEDED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setFocusable(true);
            setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
        }


        setupAppearanceSettingsView(popupView);
        updateColors();
    }

    public void setReadingAppearanceCallback(ReadingAppearanceCallback readingAppearanceCallback) {
        this.readingAppearanceCallback = readingAppearanceCallback;
    }

    private void setupAppearanceSettingsView(View popUpView) {
        RecyclerView fontChooserRecyclerView = popUpView.findViewById(R.id.fontChooser);

        currentFontSize.setText(String.valueOf(readingPrefs.contentFontSize));
        currentLineHeight.setText(String.format("%1.1f", readingPrefs.lineHeight));

        final ImageView whiteBgColor = popUpView.findViewById(R.id.bgWhiteColor);
        final ImageView merinoBgColor = popUpView.findViewById(R.id.bgMerinoColor);
        final ImageView scarpaBgColor = popUpView.findViewById(R.id.bgScarpaColor);
        final ImageView onyxBgColor = popUpView.findViewById(R.id.bgOnyxColor);

        if (readablyPrefs.autoDarkMode) {
            backgroundColorContainer.setVisibility(View.GONE);
        } else {
            backgroundColorContainer.setVisibility(View.VISIBLE);
        }


        final LinearLayout bgColorsChooserLinearLayout = popUpView.findViewById(R.id.backgroundColorContainer);

        // Restore last chosen bg color by adding a check mark to the respective bg color chooser button
        for (int i = 0; i < bgColorsChooserLinearLayout.getChildCount(); i++) {
            ImageView bgChooser = (ImageView) bgColorsChooserLinearLayout.getChildAt(i);

            if (i == readingPrefs.selectedBgIndex) {
                bgChooser.setImageResource(R.drawable.ic_check_black_24dp);
            } else {
                bgChooser.setImageResource(android.R.color.transparent);
            }
        }


        // Restore last justification by highlighting the chosen button
        if (readingPrefs.justification.equals(JUSTIFY_ALIGNMENT)) {
            justifyText.setActivated(true);
            alignLeftText.setActivated(false);
        } else if (readingPrefs.justification.equals(LEFT_ALIGNMENT)) {
            justifyText.setActivated(false);
            alignLeftText.setActivated(true);
        }


        View.OnClickListener justificationButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == justifyText.getId()) {
                    justifyText.setActivated(true);
                    alignLeftText.setActivated(false);
                    readingPrefs.setStringPref(ReadingPrefs.JUSTIFICATION_PREF_KEY, JUSTIFY_ALIGNMENT);
                } else if (v.getId() == alignLeftText.getId()) {
                    justifyText.setActivated(false);
                    alignLeftText.setActivated(true);
                    readingPrefs.setStringPref(ReadingPrefs.JUSTIFICATION_PREF_KEY, LEFT_ALIGNMENT);
                }


                if (readingAppearanceCallback != null) {
                    readingAppearanceCallback.onJustificationChanged(readingPrefs.justification);
                }
            }
        };

        View.OnClickListener lineHeightButtonClickListener = v -> {
            float lastLineHeight = readingPrefs.lineHeight;


            if (v.getId() == increaseLineHeight.getId()) {
                lastLineHeight = lastLineHeight + 0.1f;
            } else if (v.getId() == decreaseLineHeight.getId()) {
                lastLineHeight = lastLineHeight - 0.1f;
            }

            if (lastLineHeight >= LINE_HEIGHT_MAX_LIMIT ||
                    lastLineHeight <= LINE_HEIGHT_MIN_LIMIT) return;

            readingPrefs.setFloatPref(ReadingPrefs.LINE_HEIGHT_PREF_KEY, lastLineHeight);

            if (readingAppearanceCallback != null) {
                readingAppearanceCallback.onLineHeightChanged(readingPrefs.lineHeight);
            }

            currentLineHeight.setText(String.format("%1.1f", readingPrefs.lineHeight));
        };

        View.OnClickListener fontSizeButtonClickListener = v -> {
            int lastContentFontSize = readingPrefs.contentFontSize;
            int lastTitleFontSize = readingPrefs.titleFontSize;
            int articleFontSize = readingPrefs.articleInfoFontSize;


            if (v.getId() == increaseFontSize.getId()) {
                lastTitleFontSize = lastTitleFontSize + 1;
                lastContentFontSize = lastContentFontSize + 1;
                articleFontSize = articleFontSize + 1;
            } else if (v.getId() == decreaseFontSize.getId()) {
                lastTitleFontSize = lastTitleFontSize - 1;
                lastContentFontSize = lastContentFontSize - 1;
                articleFontSize = articleFontSize - 1;
            }

//            if (articleFontSize >= FONT_SIZE_MAX_LIMIT
//                    || articleFontSize <= FONT_SIZE_MIN_LIMIT) return;

            readingPrefs.setIntPref(ReadingPrefs.ARTICLE_INFO_FONT_SIZE_PREF_KEY, articleFontSize);
            readingPrefs.setIntPref(ReadingPrefs.TITLE_FONT_SIZE_PREF_KEY, lastTitleFontSize);
            readingPrefs.setIntPref(ReadingPrefs.CONTENT_FONT_SIZE_PREF_KEY, lastContentFontSize);


            if (readingAppearanceCallback != null) {
                readingAppearanceCallback.onFontSizeChanged(
                        readingPrefs.titleFontSize,
                        readingPrefs.contentFontSize,
                        readingPrefs.articleInfoFontSize
                );
            }

            currentFontSize.setText(String.valueOf(lastContentFontSize));
        };


        View.OnClickListener handleBgColorChooser = new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                whiteBgColor.setImageResource(android.R.color.transparent);
                merinoBgColor.setImageResource(android.R.color.transparent);
                scarpaBgColor.setImageResource(android.R.color.transparent);
                onyxBgColor.setImageResource(android.R.color.transparent);

                ((ImageView) v).setImageResource(R.drawable.ic_check_black_24dp);

                switch (v.getId()) {
                    case R.id.bgWhiteColor:
                        saveBgPrefs(
                                context.getString(R.string.white),
                                context.getString(R.string.white_bg_text_color),
                                context.getString(R.string.white_bg_link_color),
                                0);

                        if (readingAppearanceCallback != null) {
                            readingAppearanceCallback.onBackgroundColorChanged(
                                    context.getString(R.string.white),
                                    context.getString(R.string.white_bg_text_color),
                                    context.getString(R.string.white_bg_link_color),
                                    false,
                                    R.color.white
                            );
                        }
                        break;
                    case R.id.bgMerinoColor:

                        saveBgPrefs(
                                context.getString(R.string.merino),
                                context.getString(R.string.merino_bg_text_color),
                                context.getString(R.string.merino_bg_link_color),
                                1
                        );

                        if (readingAppearanceCallback != null) {
                            readingAppearanceCallback.onBackgroundColorChanged(
                                    context.getString(R.string.merino),
                                    context.getString(R.string.merino_bg_text_color),
                                    context.getString(R.string.merino_bg_link_color),
                                    false,
                                    R.color.merino
                            );
                        }

                        break;

                    case R.id.bgScarpaColor:

                        saveBgPrefs(
                                context.getString(R.string.scarpa_flow),
                                context.getString(R.string.scarpa_flow_bg_text_color),
                                context.getString(R.string.scarpa_flow_bg_link_color),
                                2
                        );


                        if (readingAppearanceCallback != null) {
                            readingAppearanceCallback.onBackgroundColorChanged(
                                    context.getString(R.string.scarpa_flow),
                                    context.getString(R.string.scarpa_flow_bg_text_color),
                                    context.getString(R.string.scarpa_flow_bg_link_color),
                                    true,
                                    R.color.scarpa_flow
                            );
                        }

                        break;

                    case R.id.bgOnyxColor:
                        saveBgPrefs(
                                context.getString(R.string.onyx),
                                context.getString(R.string.onyx_bg_text_color),
                                context.getString(R.string.onyx_bg_link_color),
                                3
                        );

                        if (readingAppearanceCallback != null) {
                            readingAppearanceCallback.onBackgroundColorChanged(
                                    context.getString(R.string.onyx),
                                    context.getString(R.string.onyx_bg_text_color),
                                    context.getString(R.string.onyx_bg_link_color),
                                    false,
                                    R.color.onyx
                            );
                        }

                        break;
                }
            }
        };

        whiteBgColor.setOnClickListener(handleBgColorChooser);
        merinoBgColor.setOnClickListener(handleBgColorChooser);
        scarpaBgColor.setOnClickListener(handleBgColorChooser);
        onyxBgColor.setOnClickListener(handleBgColorChooser);

        increaseFontSize.setOnClickListener(fontSizeButtonClickListener);
        decreaseFontSize.setOnClickListener(fontSizeButtonClickListener);

        increaseLineHeight.setOnClickListener(lineHeightButtonClickListener);
        decreaseLineHeight.setOnClickListener(lineHeightButtonClickListener);

        justifyText.setOnClickListener(justificationButtonClickListener);
        alignLeftText.setOnClickListener(justificationButtonClickListener);

        fontChooserAdapter = new FontChooserAdapter(context);
        fontChooserAdapter.setFontSelectedCallback(this);
        fontChooserRecyclerView.setAdapter(fontChooserAdapter);
        fontChooserRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        fontChooserRecyclerView.scrollToPosition(readingPrefs.selectedFontIndex);
    }

    @Override
    public void onFontSelected(String fontName) {
        if (readingAppearanceCallback != null) {
            readingAppearanceCallback.onFontChosen(fontName);
        }
    }

    private void saveBgPrefs(String bgColorRes, String textColorRes, String linkColorRes, int index) {
        readingPrefs.setStringPref(ReadingPrefs.BACKGROUND_COLOR_PREF_KEY, bgColorRes);
        readingPrefs.setStringPref(ReadingPrefs.TEXT_COLOR_PREF_KEY, textColorRes);
        readingPrefs.setStringPref(ReadingPrefs.LINK_COLOR_PREF_KEY, linkColorRes);
        readingPrefs.setIntPref(ReadingPrefs.SELECTED_BG_INDEX_PREF_KEY, index);

        updateColors();
        fontChooserAdapter.applyColors();
    }

    private void updateColors() {
        // Views to modify according to bg color change

        if (readingPrefs.backgroundColor.equals(context.getString(R.string.white))) {
            container.setBackgroundColor(context.getResources().getColor(R.color.white));

            justifyText.setBackground(context.getDrawable(R.drawable.reading_appearance_justification_opts_bg));
            justifyText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list));
            if (!justifyText.isActivated())
                justifyText.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            alignLeftText.setBackground(context.getDrawable(R.drawable.reading_appearance_justification_opts_bg));
            alignLeftText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list));
            if (!alignLeftText.isActivated())
                alignLeftText.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            decreaseFontSize.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            increaseFontSize.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            decreaseLineHeight.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);
            increaseLineHeight.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            currentFontSize.setTextColor(context.getResources().getColor(R.color.black));
            currentLineHeight.setTextColor(context.getResources().getColor(R.color.black));
            openReadingPrefsImageView.setColorFilter(context.getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);

            autoDarkModeDescriptionTextView.setTextColor(context.getResources().getColor(R.color.black));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                autoDarkModeSwitch.setThumbTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
                autoDarkModeSwitch.setTrackTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.black)));
            }

        } else if (readingPrefs.backgroundColor.equals(context.getString(R.string.merino))) {
            container.setBackgroundColor(context.getResources().getColor(R.color.merino));

            justifyText.setBackground(context.getDrawable(R.drawable.reading_appearance_justification_opts_bg));
            justifyText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list));
            if (!justifyText.isActivated())
                justifyText.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);

            alignLeftText.setBackground(context.getDrawable(R.drawable.reading_appearance_justification_opts_bg));
            alignLeftText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list));
            if (!alignLeftText.isActivated())
                alignLeftText.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);

            decreaseFontSize.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);
            increaseFontSize.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);

            decreaseLineHeight.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);
            increaseLineHeight.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);

            currentFontSize.setTextColor(context.getResources().getColor(R.color.irish_coffee));
            currentLineHeight.setTextColor(context.getResources().getColor(R.color.irish_coffee));
            openReadingPrefsImageView.setColorFilter(context.getResources().getColor(R.color.irish_coffee), PorterDuff.Mode.SRC_ATOP);

            autoDarkModeDescriptionTextView.setTextColor(context.getResources().getColor(R.color.irish_coffee));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                autoDarkModeSwitch.setThumbTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.irish_coffee)));
                autoDarkModeSwitch.setTrackTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.irish_coffee)));
            }

        } else if (readingPrefs.backgroundColor.equals(context.getString(R.string.scarpa_flow))) {
            container.setBackgroundColor(context.getResources().getColor(R.color.scarpa_flow));

            justifyText.setBackground(context.getDrawable(R.drawable.reading_appearance_justifications_opts_bg_dark));
            justifyText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list_dark));
            if (!justifyText.isActivated())
                justifyText.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            alignLeftText.setBackground(context.getDrawable(R.drawable.reading_appearance_justifications_opts_bg_dark));
            alignLeftText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list_dark));
            if (!alignLeftText.isActivated())
                alignLeftText.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            decreaseFontSize.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            increaseFontSize.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            decreaseLineHeight.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            increaseLineHeight.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            currentFontSize.setTextColor(context.getResources().getColor(R.color.white));
            currentLineHeight.setTextColor(context.getResources().getColor(R.color.white));
            openReadingPrefsImageView.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            autoDarkModeDescriptionTextView.setTextColor(context.getResources().getColor(R.color.white));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                autoDarkModeSwitch.setThumbTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
                autoDarkModeSwitch.setTrackTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
            }

        } else {
            container.setBackgroundColor(context.getResources().getColor(R.color.onyx));

            justifyText.setBackground(context.getDrawable(R.drawable.reading_appearance_justifications_opts_bg_dark));
            justifyText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list_dark));
            if (!justifyText.isActivated())
                justifyText.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);


            alignLeftText.setBackground(context.getDrawable(R.drawable.reading_appearance_justifications_opts_bg_dark));
            alignLeftText.setImageTintList(context.getResources().getColorStateList(R.color.active_inactive_icon_state_list_dark));
            if (!alignLeftText.isActivated())
                alignLeftText.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            decreaseFontSize.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            increaseFontSize.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            decreaseLineHeight.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            increaseLineHeight.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            openReadingPrefsImageView.setColorFilter(context.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

            currentFontSize.setTextColor(context.getResources().getColor(R.color.white));
            currentLineHeight.setTextColor(context.getResources().getColor(R.color.white));

            autoDarkModeDescriptionTextView.setTextColor(context.getResources().getColor(R.color.white));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                autoDarkModeSwitch.setThumbTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
                autoDarkModeSwitch.setTrackTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
            }

        }
    }

    public void setAutoDarkModeSwitch(boolean checked) {
        autoDarkModeSwitch.setChecked(checked);
        if (checked) {
            backgroundColorContainer.setVisibility(View.GONE);
        } else {
            backgroundColorContainer.setVisibility(View.VISIBLE);
        }
    }


    interface ReadingAppearanceCallback {
        void onJustificationChanged(String justification);
        void onLineHeightChanged(float lineHeight);
        void onFontSizeChanged(int titleFontSize, int contentFontSize, int articleInfoFontSize);
        void onBackgroundColorChanged(String bgColor, String textColor, String linkColor, boolean isNight, int colorRes);
        void onFontChosen(String fontName);

        void onAutoDarkModeSwitchClicked(boolean enabled);
    }
}
