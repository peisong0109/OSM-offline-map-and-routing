package com.example.sy_jia.myapplication2;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String MAP_FILE = "daohang.map";
    //  private TileOverlay tileOverlay;
    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    private ArrayList<View> visiblePopups = new ArrayList<>();

    private String markerObject = "marker";

    private GraphHopper hopper;
    private LatLong start;
    private LatLong end;
    private Button button01;
    private Button button02;

    private LinearLayout mapViewLayout;
    private LinearLayout infoBar;



    private LatLong firstPoint = new LatLong(30.8764216, 121.9049154), secondPoint = new LatLong(30.8837517, 121.8985143), centerPoint = new LatLong(30.8764216, 121.9049154);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //   setContentView(this.mapView);
        //   setContentView(R.layout.activity_main);
        //   mapViewLayout = (LinearLayout)findViewById(R.id.mapViewLayout);
           infoBar = (LinearLayout) findViewById(R.id.controlLayout);
           infoBar.setVisibility(View.VISIBLE);



        grassHopperFolder = getFilesDir() + "/grasshoper3";
        grassHopperAlerterFolder = getFilesDir() + "/grassHopperAlerterFolder";
        if (!(new File(grassHopperFolder).exists())) {
            new File(grassHopperFolder).mkdir();
        }
        if (!(new File(grassHopperAlerterFolder).exists())) {
            new File(grassHopperAlerterFolder).mkdir();
        }

        //  initView();

      //    new AsyncService().execute();
        extractZips();
        setUpMap();


        //  initButton();
    }

    private void initView() {

        findViewById(R.id.help).setOnClickListener(this);
    }


  //  @Override
 /*   public void onClick(View v) {
        switch (v.getId()) {

            case R.id.help://这是需要的
                startActivity(new Intent(this,Main2Activity.class));
                break;
        }
    }*/

    private void initButton() {
        //   button01 = (Button)findViewById(R.id.button11);
        //  button02 = (Button)findViewById(R.id.button22);
        //   findViewById(R.id.button11).setOnClickListener(this);
        //  findViewById(R.id.button22).setOnClickListener(this);
    }


   /* public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button11:
              //  initView();
                button01.setEnabled(false);
                button02.setEnabled(true);
                break;
            case R.id.button22:
             //   initView2();
                button02.setEnabled(false);
                button01.setEnabled(true);
                break;
        }
    }*/
   /*------------------------------------每个按键的作用--------------------------------*/
     @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonGps:
              //  initView();
             //   startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.controlZoomIn:
             //   initView2();
                startActivity(new Intent(this,Main2Activity.class));
                break;
            case R.id.controlZoomOut:
                //   initView2();

                break;
        }
    }



    private void extractZips() {
        copyAssets();
        unpackZip(grassHopperFolder, "grasshoper3.zip");
    }


    private boolean unpackZip(String path, String zipname) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(path +"/"+ zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path +"/"+ filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path +"/"+ filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    String grassHopperOsmFile;
    String grassHopperFolder;
    String grassHopperAlerterFolder;

    private void getDirections() {
        // create one GraphHopper instance
        hopper = new GraphHopper().forMobile();
        //		hopper.setDataReaderFile(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm");
        //		grassHopperOsmFile = new File(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm").getAbsolutePath();
        //		hopper.setDataReaderFile(grassHopperOsmFile);

        // where to store graphhopper files?


        hopper.setGraphHopperLocation(grassHopperFolder);

        // now this can take minutes if it imports or a few seconds for loading
        // of course this is dependent on the area you import
        hopper.load(grassHopperFolder);

        GHRequest req = new GHRequest(firstPoint.latitude, firstPoint.longitude, secondPoint.latitude, secondPoint.longitude).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);//setWeighting("fastest");
        GHResponse rsp = hopper.route(req);

        // first check for errors
        if (rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            return;
        }



        Polyline polyline = createPolyline(rsp);

        this.mapView.getLayerManager().getLayers().add(polyline);

        //   mapViewLayout.addView(mapView);

        hopper.close();
        //		findAlternateDiraction();
    }


    private void findAlternateDiraction() {
        // create one GraphHopper instance
        GraphHopper hopper = new GraphHopper().forMobile();
        //		hopper.setDataReaderFile(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm");
        //		grassHopperOsmFile = new File(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm").getAbsolutePath();
        //		hopper.setDataReaderFile(grassHopperOsmFile);

        // where to store graphhopper files?
        if (!(new File(grassHopperAlerterFolder).exists())) {
            new File(grassHopperAlerterFolder).mkdir();
        }
        hopper.setGraphHopperLocation(grassHopperAlerterFolder);

        // now this can take minutes if it imports or a few seconds for loading
        // of course this is dependent on the area you import
        hopper.setCHEnabled(false);
        hopper.load(grassHopperAlerterFolder);

        GHRequest req = new GHRequest(firstPoint.latitude, firstPoint.longitude, secondPoint.latitude, secondPoint.longitude).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
        //		req.setWeighting("fastest");
        GHResponse rsp = hopper.route(req);

        // first check for errors
        if (rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            Iterator<Throwable> throwableIterator = rsp.getErrors().iterator();
            while (throwableIterator.hasNext()) {
                Throwable throwable = throwableIterator.next();
                throwable.printStackTrace();
                System.out.println("Message:" + throwable.getMessage());
            }
            return;
        }



        Iterator<PathWrapper> pathWrapperIterator = rsp.getAll().iterator();

        while (pathWrapperIterator.hasNext()) {
            PathWrapper pathWrapper = pathWrapperIterator.next();

            System.out.println("Time:" + pathWrapper.getTime());
            System.out.println("Distance:" + pathWrapper.getDistance());
        }
        hopper.close();
        //		Polyline polyline = createPolyline(rsp);
        //
        //		this.mapView.getLayerManager().getLayers().add(polyline);
        //
        //		findAlternateDiraction();
    }




    private Polyline createPolyline(GHResponse response) {
        GraphicFactory gf = AndroidGraphicFactory.INSTANCE;
        Paint paint = gf.createPaint();
        paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK));
        paint.setStyle(Style.STROKE);
        paint.setDashPathEffect(new float[]{25, 15});
        paint.setStrokeWidth(12);
        Polyline line = new Polyline(paint, AndroidGraphicFactory.INSTANCE);

        List<LatLong> geoPoints = line.getLatLongs();
        PointList tmp = response.getBest().getPoints();
        for (int i = 0; i < response.getBest().getPoints().getSize(); i++) {
            geoPoints.add(new LatLong(tmp.getLatitude(i), tmp.getLongitude(i)));
        }

        return line;
    }

    private void setUpMap() {
        AndroidGraphicFactory.createInstance(this.getApplication());
        this.mapView = new MapView(this);
      //  setContentView(this.mapView);
        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(true);

        //  mapLinearLayout = (LinearLayout)findViewById(R.id.mapLinearLayout);
        //  mapLinearLayout.addView(mapView);

        this.tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.getModel().displayModel.getTileSize(), 1f, this.mapView.getModel().frameBufferModel.getOverdrawFactor());

        MapDataStore mapDataStore = new MapFile(new File(grassHopperFolder, MAP_FILE));
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, this.mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE)
        {
            @Override
            public boolean onLongPress(LatLong tapLatLong, Point layerXY, Point tapXY) {
                return onMapTap(tapLatLong, layerXY, tapXY);
            }
        };
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
        /*---------------------通过下面两行，既可以显示出地图，又可以显示出按键-----------------*/
           mapViewLayout = (LinearLayout)findViewById(R.id.mapViewLayout);
           mapViewLayout.addView(mapView);

        this.mapView.setCenter(new LatLong(firstPoint.latitude, firstPoint.longitude));
        createPositionMarker(firstPoint.latitude, firstPoint.longitude);
        this.mapView.setZoomLevel((byte) 17);
    }

    @Override
    protected void onDestroy() {
        this.mapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    private void createPositionMarker(double paramDouble1, double paramDouble2) {
        addMarkerPopup(paramDouble1, paramDouble2);
    }

    private void addMarkerPopup(double paramDouble1, double paramDouble2) {
        LatLong latLong = new LatLong(paramDouble1, paramDouble2);
        final View popUp = getLayoutInflater().inflate(R.layout.map_popup, mapView, false);
        popUp.findViewById(R.id.ivMarker).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                View popupview = popUp.findViewById(R.id.llMarkerData);
                visiblePopups.add(popupview);
                popupview.setVisibility(View.VISIBLE);
            }
        });
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                ////  Log.d("mapview", "onTouch");
                for (int index = 0; index < visiblePopups.size(); index++) {
                    visiblePopups.get(index).setVisibility(View.GONE);
                    visiblePopups.remove(index--);
                }
                return false;
            }
        });
        MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                latLong,
                MapView.LayoutParams.Alignment.BOTTOM_CENTER);

        mapView.addView(popUp, mapParams);
    }


    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null)
            for (String filename : files) {
                if(!filename.equals("grasshoper3.zip"))
                    continue;
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(filename);
                    File outFile = new File(grassHopperFolder, filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    class AsyncService  extends AsyncTask<Void,Void,Void> {

        @Override protected Void doInBackground(Void... voids) {
            extractZips();
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //  setUpMap();
            getDirections();
            //  mapViewLayout.addView(mapView);
        }
    }

    private Marker createMarker(LatLong p, int resource )
    {
        Drawable drawable = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawable);
        return new Marker(p, bitmap, 0, -bitmap.getHeight() / 2);
    }

    public boolean onMapTap(LatLong tapLatLong, Point layerXY, Point tapXY)
    {

        Layers layers = mapView.getLayerManager().getLayers();

        if (start != null && end == null)
        {
            end = tapLatLong;
            secondPoint = end;
            ////  shortestPathRunning = true;
            Marker marker = createMarker(tapLatLong, R.drawable.ic_place_blue_36dp);
            if (marker != null)
            {
                layers.add(marker);
            }
            new AsyncService().execute();


        }
        else
        {
            //  setInfoBarVisible(false);
            start = tapLatLong;
            firstPoint = start;
            end = null;
            secondPoint = end;
            // remove all layers but the first one, which is the map
            removeLayersExceptMap();

            Marker marker = createMarker(start, R.drawable.ic_place_red_36dp);
            if (marker != null)
            {
                layers.add(marker);
            }
        }
        return true;
    }

    public void removeLayersExceptMap(){
        Layers layers = mapView.getLayerManager().getLayers();
        while (layers.size() > 1)
        {
            layers.remove(1);
        }
    }




    //双击返回键退出程序
    private static boolean mBackKeyPressed = false;//记录是否有首次按键

    @Override
    public void onBackPressed() {
        if(!mBackKeyPressed){
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mBackKeyPressed = true;
            new Timer().schedule(new TimerTask() {//延时两秒，如果超出则擦错第一次按键记录

                @Override
                public void run() {
                    mBackKeyPressed = false;
                }
            }, 2000);
        }
        else{//退出程序
            this.finish();
            System.exit(0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.level2:
                //  newGame();
                startActivity(new Intent(this,Main2Activity.class));

                return true;
            case R.id.help:
                // showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
