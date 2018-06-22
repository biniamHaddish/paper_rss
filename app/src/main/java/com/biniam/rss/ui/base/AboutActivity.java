package com.biniam.rss.ui.base;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.biniam.rss.BuildConfig;
import com.biniam.rss.R;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AboutActivity extends AppCompatActivity {

    private TextView versionInfoTextView;
    private TextView authorLineTextView;
    private Button sendFeedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        versionInfoTextView = findViewById(R.id.versionInfo);
        authorLineTextView = findViewById(R.id.biniamAnd);
        sendFeedbackButton = findViewById(R.id.sendFeedback);

        SpannableString content = new SpannableString(getText(R.string.send_feedback));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        sendFeedbackButton.setText(content);

        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                String body = null;
                String carrierName = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getNetworkOperatorName();
                try {
                    body = String.format(getString(R.string.feedback_message_body),
                            Build.MANUFACTURER,
                            Build.MODEL,
                            carrierName,
                            Build.VERSION.SDK_INT,
                            getPackageManager().getPackageInfo(getPackageName(), 0).versionName,
                            getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_support)});
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                i.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(i, getString(R.string.send_feedback)));
            }
        });

        String versionInfo = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";
        versionInfoTextView.setText(versionInfo);


        SpannableString authorTextSpannableString = new SpannableString(getString(R.string.biniam));
        Pattern biniamRegex = Pattern.compile(getString(R.string.biniam), Pattern.CASE_INSENSITIVE);
        Matcher biniamMatcher = biniamRegex.matcher(authorTextSpannableString);

        if (biniamMatcher.find()) {
            ClickableSpan openTwitter = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Uri twitterUri = Uri.parse("https://twitter.com/intent/user?screen_name=benhaddish");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(twitterUri);
                    startActivity(intent);
                }
            };

            authorTextSpannableString.setSpan(openTwitter, biniamMatcher.start(), biniamMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        Pattern Regex = Pattern.compile(getString(R.string.), Pattern.CASE_INSENSITIVE);
        Matcher Matcher = Regex.matcher(authorTextSpannableString);

        if (Matcher.find()) {
            ClickableSpan openTwitter = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    Uri twitterUri = Uri.parse("https://twitter.com/intent/user?screen_name=isumat");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(twitterUri);
                    startActivity(intent);
                }
            };

            authorTextSpannableString.setSpan(openTwitter, Matcher.start(), Matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        authorLineTextView.setMovementMethod(LinkMovementMethod.getInstance());
        authorLineTextView.setText(authorTextSpannableString);

        LibsBuilder libsBuilder = new LibsBuilder()
                .withAutoDetect(true)
                .withFields(R.string.class.getFields())
                .withActivityStyle(Libs.ActivityStyle.LIGHT)
                .withAboutDescription(getString(R.string.app_description));

        Fragment libsFragment = libsBuilder.fragment();

        getFragmentManager().beginTransaction().replace(R.id.libs, libsFragment).commit();
    }
}
