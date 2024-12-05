package com.example.favorite_map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment implements OnMapReadyCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BlankFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(String param1, String param2) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private MapView mMapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*View rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);*/



        View rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return rootView;


        // GoogleMap のコールバックメソッドを登録する
        /*mMapView.getMapAsync(onMapReadyCallback);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);*/

    }

    public static class DatabaseHelper {

        private Context context;
        private String dbName = "Sample.db";

        public DatabaseHelper(Context context) {
            this.context = context;
        }

        public SQLiteDatabase openOrCreateDatabase() {
            String dbPath = context.getDatabasePath(dbName).getPath();
            File dbFile = new File(dbPath);

            // ディレクトリが存在しない場合は作成
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            return SQLiteDatabase.openOrCreateDatabase(dbPath, null);
        }
    }



    public void resetDatabase(Context context) {
        context.deleteDatabase("MyDatabase.db");
    }

    private SQLiteDatabase db;
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        /*String str = "data/data/" + "com/example/favorite_map" + "/Sample.db";  //データベースの保存先の指定
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(str, null);  //データベースオブジェクトの生成*/
        File dbFile = getContext().getDatabasePath("Sample.db");
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        //DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        db = dbHelper.openOrCreateDatabase();
        boolean isDatabaseInitialized = dbFile.exists();
        if (!dbFile.exists()) {
            // テーブルを初期化
            String qry0 = "DROP TABLE IF EXISTS site";
            String qry1 = "CREATE TABLE site (id INTEGER PRIMARY KEY, name STRING, ido DOUBLE, keido DOUBLE, ken STRING, cost STRING, io STRING, score DOUBLE, comment STRING)";
            String qv = "CREATE VIEW site_view AS SELECT * FROM site";
            db.execSQL(qry0);
            db.execSQL(qry1);
            db.execSQL(qv);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isDatabaseInitialized", true);
            editor.apply();
        }

        /*String qry0 = "DROP TABLE IF EXISTS site";
        String qry1 = "CREATE TABLE site (id INTEGER PRIMARY KEY, name STRING, ido DOUBLE, keido DOUBLE, ken STRING, cost STRING, io STRING, score DOUBLE, comment STRING)";
        String qv = "CREATE VIEW site_view AS SELECT * FROM site";
        db.execSQL(qry0);
        db.execSQL(qry1);
        db.execSQL(qv);*/


        //resetDatabase(getContext());


        String qry3 = "SELECT * FROM site_view";  //データ選択のクエ
        //String qry3 = "SELECT * FROM site WHERE time <= 10";
        //String qry3 = "SELECT * FROM site WHERE name = '兼六園'";
        //String qry3 = "SELECT * FROM site ORDER BY time DESC";

        Cursor cr = db.rawQuery(qry3, null);  //クエリ結果をカーソルで受け取り


        // ジェスチャーによる地図の回転を許可する
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(true);

        // 東京タワーの緯度・経度
        LatLng tokyoTower = new LatLng(36.594606,136.625669);
        //LatLng uenoPark = new LatLng(35.715329, 139.773549);


        /*String[] timei = {"東京タワー", "上野公園"};
        LatLng[] locations = {
                new LatLng(35.715329, 139.773549),  // Ueno Park
                // 他の場所の座標を追加する場合はここに追加
                new LatLng(35.689487, 139.691711),  // Example: Tokyo Tower
                new LatLng(34.693738, 135.502165)   // Example: Osaka Castle
        };*/

        //String[] time = {};
        //LatLng[] locations = {};

        ArrayList<String> timei = new ArrayList<String>(/*Arrays.asList(time)*/);
        ArrayList<LatLng> location = new ArrayList<LatLng>(/*Arrays.asList(locations)*/);


        while(cr.moveToNext()){  //カーソルを一つづつ動かしデータを取得
            int i = cr.getColumnIndex("id");  //データをテーブルの要素ごとに取得
            int n = cr.getColumnIndex("name");
            int z = cr.getColumnIndex("ido");
            int k = cr.getColumnIndex("keido");
            int id = cr.getInt(i);
            String name = cr.getString(n);
            Double ido = cr.getDouble(z);
            Double keido = cr.getDouble(k);
            timei.add(name);
            location.add(new LatLng(ido, keido));
        }

        db.close();  //データベースのクローズ

        String[] time = new String[timei.size()];
        time = timei.toArray(time);
        LatLng[] locations = new LatLng[location.size()];
        locations = location.toArray(locations);

        for (int t = 0; t < timei.size(); t++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(locations[t]);
            markerOptions.title(time[t]);
            googleMap.addMarker(markerOptions);
        }


        // 東京タワーを中心にして地図を表示する
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(tokyoTower));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(8.0f));

        // ジェスチャーによる地図の移動開始リスナを用意する
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {

                // カメラ移動の開始要因を求める
                String reason = "";
                switch(i) {
                    case GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE:
                        reason = "GESTURE";             break;
                    case GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION:
                        reason = "API_ANIMATION";       break;
                    case GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION:
                        reason = "DEVELOPER_ANIMATION"; break;
                }
                Toast.makeText(requireActivity(),
                        "CameraMove Started: " + reason, Toast.LENGTH_SHORT).show();
            }
        });

        // ジェスチャーによる地図の移動中リスナを用意する
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                // カメラ位置を求める
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                LatLng latLng = cameraPosition.target;
                float zoom    = cameraPosition.zoom;
                float bearing = cameraPosition.bearing;

                String state = String.format("%7.3f %7.3f %7.3f %7.3f°",
                        latLng.latitude, latLng.longitude, zoom, bearing);
                Log.d("CameraMove", state); //トーストだとイベント発生が多すぎて追従できない
            }
        });

        // ジェスチャーによる地図の移動終了リスナを用意する
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                // カメラ位置を求める
                CameraPosition cameraPosition = googleMap.getCameraPosition();
                LatLng latLng = cameraPosition.target;
                float zoom    = cameraPosition.zoom;
                float bearing = cameraPosition.bearing;

                String state = String.format("%7.3f %7.3f %7.3f %7.3f°",
                        latLng.latitude, latLng.longitude, zoom, bearing);
                Toast.makeText(requireActivity(),
                        "CameraMove in Idle:\n" + state, Toast.LENGTH_SHORT).show();

                // ここはお好みで追加する
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                Log.d("LatLngBounds", String.format("%7.3f,%7.3f - %7.3f,%7.3f",
                        bounds.northeast.latitude, bounds.northeast.longitude,
                        bounds.southwest.latitude, bounds.southwest.longitude));

            }
        });
    }


}