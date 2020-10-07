package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.R;

public class CardActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((R.layout.activity_card));
        initToolbarBringBack(R.string.read_card);
        initView();
    }

    private void initView() {
        View view = findViewById(R.id.card_mag);
        TextView leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag);

        view = findViewById(R.id.card_mag_enc);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mag_enc);

        view = findViewById(R.id.card_ic);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_ic);

        view = findViewById(R.id.card_m1);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_m1);

        view = findViewById(R.id.card_m1_psam);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_m1_psame);

        view = findViewById(R.id.card_sam);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_sam);

        view = findViewById(R.id.card_MIFARE_Ultralight);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_MIFARE_Ultralight);

        view = findViewById(R.id.card_MIFARE_Ultralight_ev1);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_MIFARE_Ultralight_ev1);

        view = findViewById(R.id.card_FELICA);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_FELICA);

        view = findViewById(R.id.card_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_apdu);

        view = findViewById(R.id.card_transmit_apdu);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_transmit_apdu);

        view = findViewById(R.id.card_mifare_plus);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_mifare_plus);

        view = findViewById(R.id.card_sle4442);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_sle4442_4428);

        view = findViewById(R.id.card_at24c);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_at24c);

        view = findViewById(R.id.card_at88sc);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_at88scxx);

        view = findViewById(R.id.card_ctx512);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_test_ctx512);

        view = findViewById(R.id.card_ctr_code_test);
        leftText = view.findViewById(R.id.left_text);
        view.setOnClickListener(this);
        leftText.setText(R.string.card_ctr_code_multi_apdu_test);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.card_mag:
                openActivity(MagActivity.class);
                break;
            case R.id.card_mag_enc:
                openActivity(MagEncActivity.class);
                break;
            case R.id.card_ic:
                openActivity(ICActivity.class);
                break;
            case R.id.card_m1:
                openActivity(M1Activity.class);
                break;
            case R.id.card_m1_psam:
                openActivity(MifareAuthedByPSAMActivity.class);
                break;
            case R.id.card_sam:
                openActivity(SAMActivity.class);
                break;
            case R.id.card_MIFARE_Ultralight:
                openActivity(MifareUltralightCActivity.class);
                break;
            case R.id.card_MIFARE_Ultralight_ev1:
                openActivity(MifareUtralightEv1Activity.class);
                break;
            case R.id.card_FELICA:
                openActivity(FELICAActivity.class);
                break;
            case R.id.card_apdu:
                openActivity(NormalApduActivity.class);
                break;
            case R.id.card_transmit_apdu:
                openActivity(TransmitApduActivity.class);
                break;
            case R.id.card_mifare_plus:
                openActivity(MifarePlusActivity.class);
                break;
            case R.id.card_sle4442:
                openActivity(SLE4442_4428Actviity.class);
                break;
            case R.id.card_at24c:
                openActivity(AT24CActivity.class);
                break;
            case R.id.card_at88sc:
                openActivity(AT88SCActivity.class);
                break;
            case R.id.card_ctx512:
                openActivity(CTX512Activity.class);
                break;
            case R.id.card_ctr_code_test:
                openActivity(CtrCodeAndMultiApduActivity.class);
                break;
        }
    }


}
