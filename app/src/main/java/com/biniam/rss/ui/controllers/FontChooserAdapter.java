package com.biniam.rss.ui.controllers;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biniam.rss.R;
import com.biniam.rss.persistence.preferences.ReadingPrefs;
import com.biniam.rss.utils.TemplateExtractor;

import java.io.File;

/**
 * Created by biniam_haddish on 8/5/17.
 */

public class FontChooserAdapter extends RecyclerView.Adapter<FontChooserAdapter.FontChooserViewHolder> {

    public static final String TAG = FontChooserAdapter.class.getSimpleName();

    public int selectedPosition = 0;
    private SparseArray<Typeface> typefaceSparseArray = new SparseArray<>();
    private File templatesDir;
    private String[] fontNames;
    private String[] fontPaths;
    private Context context;
    private FontSelectedCallback fontSelectedCallback;
    private ReadingPrefs readingPrefs;

    public FontChooserAdapter(Context context) {
        this.context = context;
        templatesDir = new File(context.getFilesDir(), TemplateExtractor.ASSET_EXTRACTION_DESTINATION);
        readingPrefs = ReadingPrefs.getInstance(context);
        fontNames = context.getResources().getStringArray(R.array.font_names);
        fontPaths = context.getResources().getStringArray(R.array.font_paths);

        for (int i = 0; i < fontPaths.length; i++) {
            //Log.d(TAG, String.format("FontChooserAdapter: fontpath is %s", fontPaths[i]));
            File fontPath = new File(templatesDir, fontPaths[i] + "/");

            Log.d(TAG, String.format("FontChooserAdapter: fontpath abs path %s", fontPath.getAbsolutePath()));

            for (String fontName : fontPath.list()) {
                if (fontName.contains("regular")) {
                    typefaceSparseArray.put(i, Typeface.createFromFile(new File(fontPath, fontName)));
                }
            }

            if (fontNames[i].equals(readingPrefs.selectedFontName)) {
                selectedPosition = i;
            }


        }
    }

    public void setFontSelectedCallback(FontSelectedCallback fontSelectedCallback) {
        this.fontSelectedCallback = fontSelectedCallback;
    }

    @Override
    public FontChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FontChooserViewHolder(LayoutInflater.from(context).inflate(R.layout.font_showcase_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(FontChooserViewHolder fontChooserViewHolder, int position) {
        fontChooserViewHolder.bind(typefaceSparseArray.get(position), fontNames[position]);
    }

    @Override
    public int getItemCount() {
        return fontNames.length;
    }

    public void applyColors() {
        notifyDataSetChanged();
    }

    public interface FontSelectedCallback {
        void onFontSelected(String fontName);
    }

    public class FontChooserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout fontShowCaseContainer;
        public TextView fontNameTextView;
        public TextView fontShowCaseTextView;

        public FontChooserViewHolder(View itemView) {
            super(itemView);

            if (getAdapterPosition() == 0) {
                RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams) itemView.getLayoutParams());
                params.setMarginStart(200);
                itemView.setLayoutParams(params);
            }

            fontShowCaseContainer = itemView.findViewById(R.id.fontShowCaseContainer);
            fontShowCaseContainer.setOnClickListener(this);
            this.fontNameTextView = itemView.findViewById(R.id.fontName);
            this.fontShowCaseTextView = itemView.findViewById(R.id.fontShowCase);
        }

        public void bind(Typeface typeface, String fontName) {
            fontShowCaseTextView.setTypeface(typeface);
            fontNameTextView.setText(fontName);

            boolean isSelected = fontName.equals(readingPrefs.selectedFontName);

            if (readingPrefs.backgroundColor.equals(context.getString(R.string.white))) {
                fontNameTextView.setTextColor(context.getResources().getColor(R.color.black));
                fontShowCaseContainer.setBackground(context.getDrawable(R.drawable.reading_appearance_font_chooser_bg));
                fontShowCaseTextView.setTextColor(isSelected ?
                                context.getResources().getColor(R.color.white) :
                                context.getResources().getColor(R.color.black)
                );
            } else if (readingPrefs.backgroundColor.equals(context.getString(R.string.merino))) {
                fontNameTextView.setTextColor(context.getResources().getColor(R.color.irish_coffee));
                fontShowCaseContainer.setBackground(context.getDrawable(R.drawable.reading_appearance_font_chooser_bg));
                fontShowCaseTextView.setTextColor(isSelected ?
                                context.getResources().getColor(R.color.white) :
                                context.getResources().getColor(R.color.irish_coffee)
                );

            } else if (readingPrefs.backgroundColor.equals(context.getString(R.string.scarpa_flow))) {
                fontNameTextView.setTextColor(context.getResources().getColor(R.color.white));
                fontShowCaseContainer.setBackground(context.getDrawable(R.drawable.reading_appearance_font_chooser_bg_dark));
                fontShowCaseTextView.setTextColor(isSelected ?
                                context.getResources().getColor(R.color.white) :
                                context.getResources().getColor(R.color.white)
                );

            } else {
                fontNameTextView.setTextColor(context.getResources().getColor(R.color.white));
                fontShowCaseContainer.setBackground(context.getDrawable(R.drawable.reading_appearance_font_chooser_bg_dark));
                fontShowCaseTextView.setTextColor(
                        isSelected ?
                                context.getResources().getColor(R.color.white) :
                                context.getResources().getColor(R.color.white)
                );
            }

            fontShowCaseContainer.setActivated(isSelected);
        }

        @Override
        public void onClick(View view) {
            readingPrefs.setStringPref(ReadingPrefs.SELECTED_FONT_NAME_PREF_KEY, fontNameTextView.getText().toString());
            readingPrefs.setIntPref(ReadingPrefs.SELECTED_FONT_INDEX_PREF_KEY, getAdapterPosition());

            notifyDataSetChanged();

            if (fontSelectedCallback != null) {
                fontSelectedCallback.onFontSelected(fontNameTextView.getText().toString());
            }

            view.setActivated(true);

            selectedPosition = getAdapterPosition();
        }
    }
}