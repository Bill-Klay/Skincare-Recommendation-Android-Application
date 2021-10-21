package com.skincare.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.SignupActivity;
import com.skincare.utilities.Loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ProgressDialog loader;

    private TextInputLayout layout_email_address, layout_password, layout_name, layout_phone;
    private TextInputEditText et_email_address, et_password, et_name, et_phone_number;

    private CheckBox cb_skin_combination, cb_dry_skin, cb_normal_skin, cb_oily_skin, cb_sensitive_skin;

    private Spinner spn_skin_tone;
    private List<String> skinToneList;
    private String selectedSkinTone = "";

    private Spinner spn_skin_type;
    private List<String> skinTypeList;
    private String selectedSkinTye = "";

    private Spinner spn_eye_color;
    private List<String> eyeColorList;
    private String selectedEyeColor = "";

    private Spinner spn_hair_color;
    private List<String> hairColorList;
    private String selectedHairColor = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initializing Views...
        initViews(view);
    }

    private void initViews(View view) {

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        cb_skin_combination = view.findViewById(R.id.cb_skin_combination);
        cb_dry_skin = view.findViewById(R.id.cb_dry_skin);
        cb_normal_skin = view.findViewById(R.id.cb_normal_skin);
        cb_oily_skin = view.findViewById(R.id.cb_oily_skin);
        cb_sensitive_skin = view.findViewById(R.id.cb_sensitive_skin);

        layout_name = view.findViewById(R.id.layout_name);
        et_name = view.findViewById(R.id.et_name);
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout_name.isErrorEnabled())
                    layout_name.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        layout_email_address = view.findViewById(R.id.layout_email_address);
        et_email_address = view.findViewById(R.id.et_email_address);
        et_email_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout_email_address.isErrorEnabled())
                    layout_email_address.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        layout_phone = view.findViewById(R.id.layout_phone);
        et_phone_number = view.findViewById(R.id.et_phone_number);
        et_phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout_phone.isErrorEnabled())
                    layout_phone.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /*layout_password = view.findViewById(R.id.layout_password);
        et_password = view.findViewById(R.id.et_password);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout_password.isErrorEnabled())
                    layout_password.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                    Toast.makeText(requireContext(), "Password must not start with 0",
                            Toast.LENGTH_LONG).show();
                }
            }
        });*/

        spn_skin_tone = view.findViewById(R.id.spn_skin_tone);
        skinToneList = new ArrayList<>();

        spn_skin_tone.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view1.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });

        spn_skin_tone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSkinTone = skinToneList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getSkinTones();

        spn_skin_type = view.findViewById(R.id.spn_skin_type);
        skinTypeList = new ArrayList<>();

        spn_skin_type.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view1.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });

        spn_skin_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSkinTye = skinTypeList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getSkinTyes();

        spn_eye_color = view.findViewById(R.id.spn_eye_color);
        eyeColorList = new ArrayList<>();

        spn_eye_color.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view1.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });

        spn_eye_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEyeColor = eyeColorList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getEyeColors();

        spn_hair_color = view.findViewById(R.id.spn_hair_color);
        hairColorList = new ArrayList<>();

        spn_hair_color.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    try {
                        InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(requireActivity().getWindow().getDecorView().getRootView().getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view1.performClick();
                    break;
                default:
                    break;
            }
            return true;
        });

        spn_hair_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHairColor = hairColorList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getHairColors();

        Button btn_update_profile = view.findViewById(R.id.btn_update_profile);
        btn_update_profile.setOnClickListener(this);

        if (currentUser != null)
            getUserDetails();
    }

    private void getSkinTones() {

        skinToneList.add("Select Skin Tone");
        skinToneList.add("Fair");
        skinToneList.add("Medium");
        skinToneList.add("Light");
        skinToneList.add("Tan");
        skinToneList.add("Dark");
        skinToneList.add("Olive");
        skinToneList.add("Porcelain");
        skinToneList.add("Deep");
        skinToneList.add("Ebony");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(requireContext(),
                R.layout.support_simple_spinner_dropdown_item, skinToneList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
                View view;
                if (position == 0) {
                    TextView textView = new TextView(getContext());
                    textView.setHeight(0);
                    textView.setVisibility(View.GONE);
                    view = textView;
                } else {
                    view = super.getDropDownView(position, null, parent);
                }
                return view;
            }
        };

        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spn_skin_tone.setAdapter(dataAdapter);
    }

    private void getSkinTyes() {

        skinTypeList.add("Select Skin Type");
        skinTypeList.add("Dry");
        skinTypeList.add("Combination");
        skinTypeList.add("Oily");
        skinTypeList.add("Normal");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(requireContext(),
                R.layout.support_simple_spinner_dropdown_item, skinTypeList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
                View view;
                if (position == 0) {
                    TextView textView = new TextView(getContext());
                    textView.setHeight(0);
                    textView.setVisibility(View.GONE);
                    view = textView;
                } else {
                    view = super.getDropDownView(position, null, parent);
                }
                return view;
            }
        };

        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spn_skin_type.setAdapter(dataAdapter);
    }

    private void getEyeColors() {

        eyeColorList.add("Select Eye Color");
        eyeColorList.add("Green");
        eyeColorList.add("Brown");
        eyeColorList.add("Blue");
        eyeColorList.add("Hazel");
        eyeColorList.add("Gray");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(requireContext(),
                R.layout.support_simple_spinner_dropdown_item, eyeColorList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
                View view;
                if (position == 0) {
                    TextView textView = new TextView(getContext());
                    textView.setHeight(0);
                    textView.setVisibility(View.GONE);
                    view = textView;
                } else {
                    view = super.getDropDownView(position, null, parent);
                }
                return view;
            }
        };

        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spn_eye_color.setAdapter(dataAdapter);
    }

    private void getHairColors() {

        hairColorList.add("Select Hair Color");
        hairColorList.add("Blonde");
        hairColorList.add("Black");
        hairColorList.add("Brunette");
        hairColorList.add("Red");
        hairColorList.add("Auburn");
        hairColorList.add("Gray");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(requireContext(),
                R.layout.support_simple_spinner_dropdown_item, hairColorList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
                View view;
                if (position == 0) {
                    TextView textView = new TextView(getContext());
                    textView.setHeight(0);
                    textView.setVisibility(View.GONE);
                    view = textView;
                } else {
                    view = super.getDropDownView(position, null, parent);
                }
                return view;
            }
        };

        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spn_hair_color.setAdapter(dataAdapter);
    }

    private void getUserDetails() {

        loader = Loader.show(requireContext());

        db.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        DocumentSnapshot document = task.getResult();

                        String email = document.getString("email");
                        et_email_address.setText(email);
                        et_email_address.setEnabled(false);

                        String name = document.getString("name");
                        et_name.setText(name);

                        String phone = document.getString("phone");
                        et_phone_number.setText(phone);

                        long combination_skin = document.getLong("combination_skin");
                        cb_skin_combination.setChecked(combination_skin == 1);

                        long dry_skin = document.getLong("dry_skin");
                        cb_dry_skin.setChecked(dry_skin == 1);

                        long normal_skin = document.getLong("normal_skin");
                        cb_normal_skin.setChecked(normal_skin == 1);

                        long oily_skin = document.getLong("oily_skin");
                        cb_oily_skin.setChecked(oily_skin == 1);

                        long sensitive_skin = document.getLong("sensitive_skin");
                        cb_sensitive_skin.setChecked(sensitive_skin == 1);

                        String eye_color = document.getString("eye_color");
                        spn_eye_color.setSelection(eyeColorList.indexOf(eye_color));
                        selectedEyeColor = eye_color;

                        String hair_color = document.getString("hair_color");
                        spn_hair_color.setSelection(hairColorList.indexOf(hair_color));
                        selectedHairColor = hair_color;

                        String skin_tone = document.getString("skin_tone");
                        spn_skin_tone.setSelection(skinToneList.indexOf(skin_tone));
                        selectedSkinTone = skin_tone;

                        String skin_type = document.getString("skin_type");
                        spn_skin_type.setSelection(skinTypeList.indexOf(skin_type));
                        selectedSkinTye = skin_type;

                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), task.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void updateProfile(String name, String phone_number){

        loader = Loader.show(requireContext());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("combination_skin", cb_skin_combination.isChecked() ? 1 : 0);
        userMap.put("dry_skin", cb_dry_skin.isChecked() ? 1 : 0);
        userMap.put("normal_skin", cb_normal_skin.isChecked() ? 1 : 0);
        userMap.put("oily_skin", cb_oily_skin.isChecked() ? 1 : 0);
        userMap.put("sensitive_skin", cb_sensitive_skin.isChecked() ? 1 : 0);
        userMap.put("eye_color", selectedEyeColor);
        userMap.put("hair_color", selectedHairColor);
        userMap.put("skin_tone", selectedSkinTone);
        userMap.put("skin_type", selectedSkinTye);
        userMap.put("name", name);
        userMap.put("phone", phone_number);
        userMap.put("userImage", "");
        userMap.put("userUid", currentUser.getUid());

        DocumentReference user_doc_ref = db.collection("Users").document(currentUser.getUid());
        user_doc_ref
                .update(userMap)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), "User Profile Updated!",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void validations() {

        String name = et_name.getText().toString().trim();
        String phone_number = et_phone_number.getText().toString().trim();

        if (name.isEmpty()) {
            layout_name.setErrorEnabled(true);
            layout_name.setError("Enter Name");
            layout_name.requestFocus();
        } else if (phone_number.isEmpty()) {
            layout_phone.setErrorEnabled(true);
            layout_phone.setError("Enter Phone Number");
            layout_phone.requestFocus();
        } else if (spn_skin_tone.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select Skin Tone",
                    Toast.LENGTH_LONG).show();
        } else if (spn_skin_type.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select Skin Type",
                    Toast.LENGTH_LONG).show();
        } else if (spn_eye_color.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select Eye Color",
                    Toast.LENGTH_LONG).show();
        } else if (spn_hair_color.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Select Hair Color",
                    Toast.LENGTH_LONG).show();
        } else {
            if (currentUser != null)
                updateProfile(
                        name,
                        phone_number
                );
        }
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btn_update_profile){
            validations();
        }
    }
}