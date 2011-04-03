//Benjamin Salazar, Trent, Rick
//FSUfm
//Project #2
//EEL4930

package edu.fsu.cs.argame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class menu extends MixView {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        ListView menuList = (ListView) findViewById(R.id.ListView_Menu);
        String[] items = { getResources().getString(R.string.first_choice),
                getResources().getString(R.string.second_choice),
                getResources().getString(R.string.third_choice),
                getResources().getString(R.string.fourth_choice) };
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, R.layout.menu_item, items);
        menuList.setAdapter(adapt);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                // Note: if the list was built "by hand" the id could be used.
                // As-is, though, each item has the same id
                TextView textView = (TextView) itemClicked;
                String strText = textView.getText().toString();
                if (strText.equalsIgnoreCase(getResources().getString(R.string.first_choice))) {
                    // Launch the Zombie Activity
                    startActivity(new Intent(menu.this, MixView.class));
                } else if (strText.equalsIgnoreCase(getResources().getString(R.string.second_choice))) {
                    // Launch the Archer Activity
                    startActivity(new Intent(menu.this, MixView.class));
                } else if (strText.equalsIgnoreCase(getResources().getString(R.string.third_choice))) {
                    // Launch the Skull Activity
                    startActivity(new Intent(menu.this, MixView.class));
                } else if (strText.equalsIgnoreCase(getResources().getString(R.string.fourth_choice))) {
                    // Launch the Help Activity
                    startActivity(new Intent(menu.this, MixView.class));
                }
            }
        });
    }
}
