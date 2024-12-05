package com.example.favorite_map;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment2 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public BlankFragment2() {
        // Required empty public constructor
    }

    public static BlankFragment2 newInstance(String param1, String param2) {
        BlankFragment2 fragment = new BlankFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            }
        }
    }

    //新規の登録
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String qry2 = "INSERT INTO site(name, ido, keido, ken, cost, io, score, comment) VALUES ('現在地', "+ latitude + ", " + longitude + ", 'null', 'null', 'null', 0, 'いい')";
                    Log.d("SQL", qry2);
                    db.execSQL(qry2);

                    // データを挿入した後にリストを更新
                    refreshList();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //ツールバーのプラス、とフィルターのアイコンを押したときの処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getLastKnownLocation();
            }

            return true;
        } else if (id == R.id.action_settings) {
            showDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4;

    //フィルターのダイアログ
    private void showDialog() {
        Context context = getContext();
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        checkBox1 = dialog.findViewById(R.id.c1);
        checkBox2 = dialog.findViewById(R.id.c2);
        checkBox3 = dialog.findViewById(R.id.c3);

        RatingBar rb = dialog.findViewById(R.id.r);

        Spinner spinner = dialog.findViewById(R.id.kenn);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.prefectures_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Spinner spinner2 = dialog.findViewById(R.id.io);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                context,
                R.array.io_array,
                android.R.layout.simple_spinner_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        Button dialogButton = dialog.findViewById(R.id.bf);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sp = spinner.getSelectedItem().toString();
                String sio = spinner2.getSelectedItem().toString();
                double rating = rb.getRating();
                boolean c1 = checkBox1.isChecked();
                boolean c2 = checkBox2.isChecked();
                boolean c3 = checkBox3.isChecked();
                String q1 = "DROP VIEW IF EXISTS site_view";
                String q2;
                if (c1 && c2 && c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where ken = '" + sp + "' && io = '" + sio + "' && score = '" + rating + "'";
                } else if (c1 && c2 && !c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where ken = '" + sp + "' && io = '" + sio + "'";
                } else if (c1 && !c2 && c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where ken = '" + sp + "' && score = '" + rating + "'";
                } else if (!c1 && c2 && c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where io = '" + sio + "' && score = '" + rating + "'";
                } else if (c1 && !c2 && !c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where ken = '" + sp + "'";
                } else if (!c1 && c2 && !c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where io = '" + sio + "'";
                } else if (!c1 && !c2 && c3) {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site where score = '" + rating + "'";
                } else {
                    q2 = "CREATE VIEW site_view AS SELECT * FROM site";
                }
                db.execSQL(q1);
                db.execSQL(q2);
                refreshList();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private ListView lview;

    //登録したもの編集
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BlankFragment.DatabaseHelper dbHelper = new BlankFragment.DatabaseHelper(getContext());
        db = dbHelper.openOrCreateDatabase();

        View rootView = inflater.inflate(R.layout.fragment_blank2, container, false);
        lview = rootView.findViewById(R.id.listview);

        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getContext();
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.input);

                EditText editname = dialog.findViewById(R.id.en);
                EditText editTextCost = dialog.findViewById(R.id.et2);
                RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
                EditText editTextComment = dialog.findViewById(R.id.et5);

                Spinner spinner = dialog.findViewById(R.id.ekenn);
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.prefectures_array,
                        android.R.layout.simple_spinner_item
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                Spinner spinner2 = dialog.findViewById(R.id.eio);
                ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                        context,
                        R.array.io_array,
                        android.R.layout.simple_spinner_item
                );
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(adapter2);

                Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
                String nemes = item.get("name");

                Button dialogButton = dialog.findViewById(R.id.button);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = editname.getText().toString();
                        String sp = spinner.getSelectedItem().toString();
                        String cost = editTextCost.getText().toString();
                        String sio = spinner2.getSelectedItem().toString();
                        double rating = ratingBar.getRating();
                        String c = editTextComment.getText().toString();
                        String q2 = "UPDATE site SET name = '" + name + "', ken = '" + sp + "', cost = '" + cost + "', io = '" + sio + "', score = " + rating + ", comment = '" + c + "' WHERE name = '" + nemes +"'";
                        db.execSQL(q2);
                        refreshList();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        lview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
                String name = item.get("name");
                Log.d("DELETE", "Deleting item with name: " + name);
                db.execSQL("DELETE FROM site WHERE name = '" + name + "'");
                refreshList();
                return true;
            }
        });

        refreshList();
        return rootView;
    }

    //リストビューの表示
    private void refreshList() {
        String qry0 = "SELECT * FROM site_view";
        Cursor cr = db.rawQuery(qry0, null);

        ArrayList<Map<String, String>> namae = new ArrayList<>();
        while (cr.moveToNext()) {
            int n = cr.getColumnIndex("name");
            int k = cr.getColumnIndex("ken");
            int co = cr.getColumnIndex("cost");
            int i = cr.getColumnIndex("io");
            int s = cr.getColumnIndex("score");
            int c = cr.getColumnIndex("comment");
            String name = cr.getString(n);
            String ken = cr.getString(k);
            String cost = cr.getString(co);
            String io = cr.getString(i);
            Double score = cr.getDouble(s);
            String comment = cr.getString(c);

            String result = "県：" + ken + "\nかかった金額：" + cost + "\n室内外：" + io + "\nスコア：" + score + "\nコメント：" + comment + "\n";

            Map<String, String> item = new HashMap<>();
            item.put("name", name);
            item.put("comment", result);
            namae.add(item);
        }

        cr.close();
        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                namae,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "comment"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        lview.setAdapter(adapter);
    }
}
