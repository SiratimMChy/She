package com.example.she;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

public class SafetyTips extends Fragment {

    public SafetyTips() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_safety_tips, container, false);

        ListView listView = view.findViewById(R.id.list_safety_tips);

        String[] safetyTips = {
                "1.Always be aware of your surroundings.",
                "2.Trust your instincts and leave situations that feel unsafe.",
                "3.Keep emergency contacts saved on speed dial.",
                "4.Share your live location with a trusted friend when traveling alone.",
                "5.Avoid walking alone in deserted or poorly lit areas at night.",
                "6.Carry a self-defense tool like pepper spray or a whistle.",
                "7.Do not share personal information with strangers.",
                "8.Always use verified and trusted cab services.",
                "9.Lock your doors and windows properly at home.",
                "10.Take self-defense classes to stay prepared."
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.safety_tip_item,
                safetyTips
        );

        listView.setAdapter(adapter);
        return view;
    }
}
