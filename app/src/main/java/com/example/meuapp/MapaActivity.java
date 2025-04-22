package com.example.meuapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MapaActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private EditText searchEditText;
    private ImageButton searchButton;
    private Button routeButton;
    private Polyline currentPolyline;
    private LatLng destinationLatLng;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private static final int REQUEST_CALL_PERMISSION = 2;
    private static final int REQUEST_CALL_PERMISSION_2 = 3;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ImageButton callButton;
    private ImageButton callButton2;
    private EditText originEditText, destinationEditText;
    private ImageButton myLocationButton;
    private ImageButton alternateRouteButton;
    private LinearLayout routeInfoPanel;
    private TextView tvDistance, tvDuration;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        searchButton = findViewById(R.id.searchButton);
        routeButton = findViewById(R.id.routeButton);
        callButton = findViewById(R.id.callButton);
        callButton2 = findViewById(R.id.callButton2);
        originEditText = findViewById(R.id.originEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        myLocationButton = findViewById(R.id.localizacao);
        alternateRouteButton = findViewById(R.id.rota);
        routeInfoPanel = findViewById(R.id.routeInfoPanel);
        tvDistance = findViewById(R.id.tvDistance);
        tvDuration = findViewById(R.id.tvDuration);

        searchButton.setOnClickListener(v -> searchLocation());
        routeButton.setOnClickListener(v -> drawRoute());
        callButton.setOnClickListener(v -> makePhoneCall("20797429"));
        callButton2.setOnClickListener(v -> makePhoneCall("20797429"));
        myLocationButton.setOnClickListener(v -> centerOnMyLocation());
        alternateRouteButton.setOnClickListener(v -> findAlternateRoute());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            enableMyLocation();
        }

        mMap.setOnMapClickListener(latLng -> {
            destinationLatLng = latLng;
            updateDestinationMarker(latLng);
            updateDestinationAddress(latLng);
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateCurrentLocationUI();
                            updateCurrentAddress(location);
                        } else {
                            Toast.makeText(this, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateCurrentLocationUI() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        mMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Você está aqui")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void updateCurrentAddress(Location location) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(), location.getLongitude(), 1);

                if (!addresses.isEmpty()) {
                    String address = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> originEditText.setText(address));
                }
            } catch (IOException e) {
                Log.e("Geocoder", "Erro ao obter endereço", e);
            }
        }).start();
    }

    private void searchLocation() {
        String destination = destinationEditText.getText().toString();
        if (destination.isEmpty()) {
            Toast.makeText(this, "Digite um destino para pesquisar", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(destination, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    LatLng destLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                    runOnUiThread(() -> {
                        destinationLatLng = destLatLng;
                        updateDestinationMarker(destLatLng);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 15));
                        Toast.makeText(this, "Destino encontrado", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Destino não encontrado", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao buscar localização", Toast.LENGTH_SHORT).show());
                Log.e("Geocoder", "Erro no geocoder", e);
            }
        }).start();
    }

    private void updateDestinationMarker(LatLng position) {
        mMap.clear();
        if (currentLocation != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Você está aqui")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    private void updateDestinationAddress(LatLng position) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        position.latitude, position.longitude, 1);

                if (!addresses.isEmpty()) {
                    String address = addresses.get(0).getAddressLine(0);
                    runOnUiThread(() -> destinationEditText.setText(address));
                }
            } catch (IOException e) {
                Log.e("Geocoder", "Erro ao obter endereço", e);
            }
        }).start();
    }

    private void drawRoute() {
        if (destinationLatLng == null || currentLocation == null) {
            Toast.makeText(this, "Defina a origem e destino primeiro", Toast.LENGTH_SHORT).show();
            return;
        }

        routeButton.setEnabled(false);
        routeButton.setText("Calculando rota...");

        new Thread(() -> {
            try {
                GeoApiContext context = new GeoApiContext.Builder()
                        .apiKey("AIzaSyC-wRnE5vxQRS7VZoe54O1MHgbvRW0BCI4")
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build();

                DirectionsResult result = DirectionsApi.newRequest(context)
                        .mode(TravelMode.DRIVING)
                        .origin(new com.google.maps.model.LatLng(
                                currentLocation.latitude,
                                currentLocation.longitude))
                        .destination(new com.google.maps.model.LatLng(
                                destinationLatLng.latitude,
                                destinationLatLng.longitude))
                        .await();

                runOnUiThread(() -> {
                    routeButton.setEnabled(true);
                    routeButton.setText("Calcular Rota");

                    if (result.routes == null || result.routes.length == 0) {
                        Toast.makeText(this, "Nenhuma rota encontrada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    processRouteResult(result);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    routeButton.setEnabled(true);
                    routeButton.setText("Calcular Rota");
                    Toast.makeText(this, "Erro ao calcular rota: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("DirectionsAPI", "Erro na API", e);
                });
            }
        }).start();
    }

    private void processRouteResult(DirectionsResult result) {
        List<LatLng> path = PolyUtil.decode(
                result.routes[0].overviewPolyline.getEncodedPath());

        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        currentPolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(path)
                .width(15)
                .color(Color.parseColor("#4285F4"))
                .geodesic(true));

        updateRouteInfo(result.routes[0].legs[0].distance.humanReadable,
                result.routes[0].legs[0].duration.humanReadable);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(currentLocation)
                .include(destinationLatLng)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(mMap.getCameraPosition().target)
                        .zoom(mMap.getCameraPosition().zoom)
                        .tilt(45)
                        .bearing(mMap.getCameraPosition().bearing)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onCancel() {}
        });
    }

    private void updateRouteInfo(String distance, String duration) {
        tvDistance.setText(distance);
        tvDuration.setText(duration);
        routeInfoPanel.setVisibility(View.VISIBLE);
    }

    private void centerOnMyLocation() {
        if (currentLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentLocation)
                    .zoom(15)
                    .tilt(45)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            Toast.makeText(this, "Localização não disponível", Toast.LENGTH_SHORT).show();
        }
    }

    private void findAlternateRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
        drawRoute();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        centerOnMyLocation();
        return true;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        updateCurrentAddress(location);
        Toast.makeText(this, "Localização atualizada", Toast.LENGTH_SHORT).show();
    }

    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Solicita permissão
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        } else {
            // Já tem permissão, faz a chamada
            startPhoneCall(phoneNumber);
        }
    }

    private void startPhoneCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Erro ao fazer chamada: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        } else if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Usar o número do primeiro botão como padrão
                startPhoneCall("20797429");
            } else {
                Toast.makeText(this, "Permissão para ligação negada", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CALL_PERMISSION_2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPhoneCall("20797429"); // Ou outro número para o segundo botão
            }
        }
    }
}