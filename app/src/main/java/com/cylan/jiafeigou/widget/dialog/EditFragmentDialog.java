package com.cylan.jiafeigou.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/13 17:30
 * 描述：在点击设备名称之后，弹出的fragment对话框
 */
public class EditFragmentDialog extends BaseDialog {


    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_dialog_title)
    TextView tvDialogTitle;
    @BindView(R.id.et_input_box)
    EditText etInputBox;
    @BindView(R.id.et_input_box_clear)
    ImageView ivClear;


    public static final String KEY_TITLE = "key_title";
    public static final String KEY_LEFT_CONTENT = "key_left";
    public static final String KEY_RIGHT_CONTENT = "key_right";
    public static final String KEY_SHOW_EDIT = "key_show_edit";
    public static final String KEY_INPUT_HINT = "key_input_hint";
    public static final String KEY_DEFAULT_EDIT_TEXT = "KEY_DEFAULT_EDIT_TEXT";
    public static final String KEY_TOUCH_OUT_SIDE_DISMISS = "key_touch_outside";


    public static EditFragmentDialog newInstance(Bundle bundle) {
        EditFragmentDialog fragment = new EditFragmentDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getCustomHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_edit_name, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ivClear.setOnClickListener(v -> etInputBox.getText().clear());
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> String.valueOf(source).replace(" ", "");
        etInputBox.setFilters(filters);
        etInputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    tvConfirm.setEnabled(false);
                    tvConfirm.setTextColor(getContext().getResources().getColor(R.color.color_979797));
                    ivClear.setVisibility(View.INVISIBLE);
                } else {
                    tvConfirm.setEnabled(true);
                    tvConfirm.setTextColor(getContext().getResources().getColor(R.color.color_4b9fd5));
                    ivClear.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        Bundle bundle = getArguments();
        final String title = bundle.getString(KEY_TITLE);
        final String lContent = bundle.getString(KEY_LEFT_CONTENT);
        final String rContent = bundle.getString(KEY_RIGHT_CONTENT);
        final String hint = bundle.getString(KEY_INPUT_HINT);
        final String text = bundle.getString(KEY_DEFAULT_EDIT_TEXT);
        if (!TextUtils.isEmpty(title))
            tvDialogTitle.setText(title);
        if (!TextUtils.isEmpty(lContent))
            tvConfirm.setText(lContent);
        if (!TextUtils.isEmpty(rContent))
            tvCancel.setText(rContent);
        if (!bundle.getBoolean(KEY_SHOW_EDIT, true)) {
            getView().findViewById(R.id.lLayout_input_box).setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(hint)) {
            etInputBox.setHint(hint);
        }
        if (!TextUtils.isEmpty(text)) {
            etInputBox.setText(text);
        }
        getDialog().setCanceledOnTouchOutside(bundle.getBoolean(KEY_TOUCH_OUT_SIDE_DISMISS, false));
        return super.show(transaction, tag);
    }

    /**
     * 点击确认按钮之后，把软键盘进行隐藏
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                //点击确认按钮之后，把软键盘进行隐藏
                hideKeyboard(view);
                dismiss();
                if (action != null) {
                    action.onDialogAction(R.id.tv_confirm, etInputBox.getText().toString());
                    etInputBox.getText().clear();
                }
                break;
            case R.id.tv_cancel:
                hideKeyboard(view);
                dismiss();
                etInputBox.getText().clear();
//                if (option != null) {
//                    option.onDialogAction(R.dpMsgId.tv_cancel, null);
//                }
                break;
        }
    }
}
