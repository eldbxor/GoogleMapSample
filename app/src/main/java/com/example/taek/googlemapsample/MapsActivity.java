package com.example.taek.googlemapsample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonPointStyle;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "MapsActivity";
    private GoogleMap mMap;
    private Geocoder geocoder;
    private Button buttonFind;
    private ImageButton buttonMarker;
    private TextView textView_item;
    private ListView listView_address;
    private EditText editAddress;
    private Context context = this;
    private MarkerOptions marker;
    private final static int LOCATION_REQUEST_CODE = 1;
    private LocationListener locationListener;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private int editable_length = 0;

    private ArrayList<String> items;
    private ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        items = new ArrayList<>();
        adapter = new ArrayAdapter(this, R.layout.item_listview, items);

        buttonFind = (Button) findViewById(R.id.button_find);
        buttonMarker = (ImageButton) findViewById(R.id.button_marker);
        editAddress = (EditText) findViewById(R.id.edit_address);
        listView_address = (ListView) findViewById(R.id.listview_address);
        textView_item = (TextView) findViewById(R.id.tv_address);
        listView_address.setAdapter(adapter);
        geocoder = new Geocoder(context);
        marker = new MarkerOptions();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        editAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        items.clear();
                        listView_address.clearChoices();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                        List<Address> list = null;
                        String str = editAddress.getText().toString();
                        if (str.length() == 0)
                            return;
                        try {
                            list = geocoder.getFromLocationName(str, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "입출력 오류 - 서버에서 주소변환 시 에러발생");
                        }

                        try {
                            if (list.size() != 0) {
                                for (Address address : list) {
                                    items.add(address.getAddressLine(0));
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

                /*
                if (editable.length() != editable_length) {
                    editable_length = editable.length();
                    Toast.makeText(context, editable.toString(), Toast.LENGTH_SHORT).show();
                }
                */
            }
        });

        buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Address> list = null;
                String str = editAddress.getText().toString();
                try {
                    list = geocoder.getFromLocationName(str, 10);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("ERROR", "입출력 오류 - 서버에서 주소변환 시 에러발생");
                }

                if (list != null) {
                    if (list.size() == 0) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "해당되는 주소 정보는 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }, 200);
                    } else {
                        Address addr = list.get(0);
                        double lat = addr.getLatitude();
                        double lon = addr.getLongitude();

                        // 마커의 위치설정 및 정보창 입력
                        if (addr.getAddressLine(0) != null) {
                            setMarker(lat, lon, addr.getAddressLine(0), "(" + addr.getLatitude() + "," + addr.getLongitude() + ")");
                        } else {
                            // 위치를 찾지 못함
                        }

                        // 지도에 반경(Radius) 그리기
                        CircleOptions circleOptions = new CircleOptions()
                                .center(new LatLng(lat, lon))
                                .strokeColor(Color.BLUE)
                                .fillColor(R.color.lightBlue)
                                .radius(200); // In meters

                        // 새로운 위치로 카메라 이동, 주위에 반경 그리기
                        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));
                        getMap().addCircle(circleOptions);
                    }
                }
            }
        });

        buttonMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Dialog에서 보여줄 입력화면 View 객체 생성 작업
                //Layout xml 리소스 파일을 View 객체로 부불려 주는(inflate) LayoutInflater 객체 생성
                LayoutInflater inflater = getLayoutInflater();

                //Dialog의 listener에서 사용하기 위해 final로 참조변수 선언
                final View dialogView = inflater.inflate(R.layout.dialog_change_marker, null);

                ImageButton imageButton1 = dialogView.findViewById(R.id.imagebutton1);
                ImageButton imageButton2 = dialogView.findViewById(R.id.imagebutton2);
                ImageButton imageButton3 = dialogView.findViewById(R.id.imagebutton3);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Change markers's design");
                builder.setIcon(R.drawable.ic_place_black_24dp);
                builder.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)

                // Cancel 버튼을 눌렀을 때
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 아무 동작도 하지 않음
                    }
                });

                // 설정한 값으로 AlertDialog 객체 생성
                final AlertDialog dialog = builder.create();

                // Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog.setCanceledOnTouchOutside(true); // 없어지도록 설정

                dialog.show();

                imageButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_24dp));
                        dialog.dismiss();
                    }
                });
                imageButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation_black_24dp));
                        dialog.dismiss();
                    }
                });
                imageButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_walk_black_24dp));
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void setMarker(double latitude, double longitude, String title, String snippet) {
        // 마커의 위치 설정
        marker.position(new LatLng(latitude, longitude));

        // 마커의 정보창 입력
        marker.title(title);
        marker.snippet(snippet);

        marker.draggable(false);

        // 기존의 마커를 지우고 새로운 위치로 마킹, 주위에 반경 그리기
        getMap().clear();
        getMap().addMarker(marker);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 위치 권한 받기
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                // Show rationale and request permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }

        // 현재위치 버튼 클릭 시
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // 마지막 위치를 찾아서 마킹함
                        Task<Location> task_location = LocationServices.getFusedLocationProviderClient(context).getLastLocation();
                        task_location.addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                // mMap.clear();
                                mLastKnownLocation = task.getResult();

                                // 역지오코딩
                                List<Address> list = null;
                                try {
                                    list = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 10);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (list.get(0) != null) {
                                    Address addr = list.get(0);
                                    if (addr.getAddressLine(0) != null) {
                                        setMarker(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), addr.getAddressLine(0), "(" + addr.getLatitude() + "," + addr.getLongitude() + ")");
                                    }
                                }

                                // 지도에 반경(Radius) 그리기
                                CircleOptions circleOptions = new CircleOptions()
                                        .center(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                        .strokeColor(Color.BLUE)
                                        .fillColor(R.color.lightBlue)
                                        .radius(mLastKnownLocation.getAccuracy()); // In meters

                                getMap().addCircle(circleOptions);
                            }
                        });
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if ( cause ==  CAUSE_NETWORK_LOST )
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED )
            Log.e(TAG,"onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected GoogleMap getMap() {
        return mMap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
            } else {
                // Permission was denied. Display an error message.
            }
        }
    }
}
