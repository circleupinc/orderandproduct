package com.circleupinc.productandorder.Product;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.circleupinc.productandorder.Interface.ItemLickListener;
import com.circleupinc.productandorder.Product.UploderUtils.FileUtil;
import com.circleupinc.productandorder.Product.UploderUtils.UploaderApi;

import com.circleupinc.productandorder.R;
import com.circleupinc.productandorder.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetails extends AppCompatActivity {



    private List<Boolean> fileNameList;
    private List<Uri> fileImage;

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int READ_STORAGE_CODE = 1001;
    private static final int WRITE_STORAGE_CODE = 1002;


    private UploadListAdapter uploadListAdapter;

    String keyString="test";
    int imageLimit = 0;


    Button btnComponent;
    EditText edtComponent, edtTitle, edtDicription;


    LinearLayout selectImage, addProduct;
    RecyclerView selectImageRecycle, recyComponent;

   //imageupload.php
    public static final String BASE_URL = " http://shub.shopappbd.com/";


    //FirebaseRecyclerAdapter<CommonModel, CommonViewHolder> adapter;


    private static UploaderApi uploaderApi;

    private static UploaderApi getUploaderApi() {

        return RetrofitClient.getClient( BASE_URL ).create( UploaderApi.class );
    }








    RelativeLayout priceSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);


        priceSection = findViewById(R.id.price_section);

        priceSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ProductDetails.this, ChangePrice.class));


            }
        });



        // image upload





        uploaderApi = getUploaderApi( );

        fileNameList = new ArrayList<>( );
        fileImage = new ArrayList<>( );

        uploadListAdapter = new UploadListAdapter( fileNameList, fileImage );


        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */

        // set image limit
        imageLimit = 10;

        // product info
        selectImage = findViewById( R.id.select_btn );
        selectImageRecycle = findViewById( R.id.upload_list );


        float scalefactor = getResources( ).getDisplayMetrics( ).density * 100;
        int number = getWindowManager().getDefaultDisplay( ).getWidth( );
        int columns = (int) ((float) number /  scalefactor);


        selectImageRecycle.setLayoutManager( new GridLayoutManager(this, columns) );

        //    selectImageRecycle.setLayoutManager( new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false ) );
        selectImageRecycle.setHasFixedSize( true );
        selectImageRecycle.setAdapter( uploadListAdapter );


        // -------update image limit---------



        selectImage.setOnClickListener( new View.OnClickListener( ) {
            @Override
            public void onClick(View view) {

                if (fileImage.size( ) < imageLimit) {
                    openImageIntent( );

                } else {

                    Toast.makeText( ProductDetails.this, "Can't upload more than " + imageLimit + "pictures", Toast.LENGTH_SHORT ).show( );

                }


            }
        } );


















        ////







    }








    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

            if (data.getClipData( ) != null || data.getData( ) != null) {
                int totalItemsSelected = 0;

                if (data.getClipData( ) != null) {

                    totalItemsSelected = data.getClipData( ).getItemCount( );
                } else {
                    totalItemsSelected = 1;
                }

                int remain = imageLimit - (fileImage.size( ));

                final int a;
                if (fileImage.size( ) == 0) {

                    a = 0;

                } else {

                    a = fileImage.size( );

                }

                if (fileImage.size( ) + totalItemsSelected <= imageLimit) {
                    if (totalItemsSelected <= imageLimit) {


                        for (int i = a; i < a + totalItemsSelected; i++) {

                            final Uri fileUri;
                            if (totalItemsSelected == 1) {
                                fileUri = data.getData( );
                            } else {
                                fileUri = data.getClipData( ).getItemAt( i - a ).getUri( );
                            }

                            String fileName = fileUri.getPath( );
                            fileNameList.add( false );
                            UploadImage( fileUri, i );
                            fileImage.add( fileUri );
                            uploadListAdapter.notifyDataSetChanged( );


                        }


                    } else {
                        Toast.makeText( ProductDetails.this, "You can't select more then " + imageLimit + " picture", Toast.LENGTH_SHORT ).show( );

                    }

                } else {
                    Toast.makeText( ProductDetails.this, "Remaining " + remain + " Photo", Toast.LENGTH_SHORT ).show( );
                }
            } else {

                Toast.makeText( ProductDetails.this, "Select  picture", Toast.LENGTH_SHORT ).show( );

            }

        }

    }

    // get image file name
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme( ).equals( "content" )) {
            Cursor cursor = getContentResolver( ).query( uri, null, null, null, null );
            try {
                if (cursor != null && cursor.moveToFirst( )) {
                    result = cursor.getString( cursor.getColumnIndex( OpenableColumns.DISPLAY_NAME ) );
                }
            } finally {
                cursor.close( );
            }
        }
        if (result == null) {
            result = uri.getPath( );
            int cut = result.lastIndexOf( '/' );
            if (cut != -1) {
                result = result.substring( cut + 1 );
            }
        }
        return result;
    }

    // image adapter
    class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder> {

        private List<Uri> fileImage;

        private List<Boolean> fileNameList;


        public UploadListAdapter(List<Boolean> fileNameList, List<Uri> fileImage) {

            this.fileNameList = fileNameList;

            this.fileImage = fileImage;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from( parent.getContext( ) );
            View itemView = inflater.inflate( R.layout.select_item_image, parent, false );
            return new ViewHolder( itemView );

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            Uri fileName = fileImage.get( position );
            Picasso.with( getBaseContext( ) ).load( fileName ).centerCrop( ).fit( ).into( holder.fileDoneView );

            if (fileNameList.get( position ).equals( true )) {

                holder.progressBar.setVisibility( View.GONE );

            } else {

                holder.progressBar.setVisibility( View.VISIBLE );
            }

            holder.setItemLickListener( new ItemLickListener( ) {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {


                    // full screen image view
                    // Intent intent = new Intent( SharePost.this, ProductImageView.class );
                    //   intent.putExtra( "id", keyString );
                    //    startActivity( intent );

                }
            } );


        }

        @Override
        public int getItemCount() {
            return fileImage.size( );
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            View mView;

            public ImageView fileDoneView;
            public ProgressBar progressBar;
            public ItemLickListener itemLickListener;

            public ViewHolder(View itemView) {
                super( itemView );

                mView = itemView;

                // fileNameView = (TextView) mView.findViewById(R.id.upload_filename);
                fileDoneView = mView.findViewById( R.id.upload_loading );
                progressBar = mView.findViewById( R.id.image_upload_progress );
                itemView.setOnClickListener( this );


            }


            public void setItemLickListener(ItemLickListener itemLickListener) {
                this.itemLickListener = itemLickListener;
            }

            @Override
            public void onClick(View view) {
                itemLickListener.onClick( view, getAdapterPosition( ), false );
            }

        }


    }

    // check permission
    private boolean isPermissionGranted(String permission) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission( this, permission );

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }


    //Requesting permission
    private void requestPermission(String permission, int code) {

        if (ActivityCompat.shouldShowRequestPermissionRationale( this, permission )) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions( this, new String[]{permission}, code );
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == READ_STORAGE_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                openImageIntent( );

            } else {

                ProductDetails.this.finish( );
            }
        } else if (requestCode == WRITE_STORAGE_CODE) {


            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                ProductDetails.this.finish( );
            }
        }
    }

    // open image list
    private void openImageIntent() {

        if (isPermissionGranted( Manifest.permission.READ_EXTERNAL_STORAGE )) {
            Intent intent = new Intent( );
            intent.setType( "image/*" );
            intent.putExtra( Intent.EXTRA_ALLOW_MULTIPLE, true );
            intent.setAction( Intent.ACTION_GET_CONTENT );
            startActivityForResult( Intent.createChooser( intent, "Select Picture" ), RESULT_LOAD_IMAGE );

        } else {
            requestPermission( Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_CODE );
        }

    }

    // upload image to firebase storage and  database
    private void UploadImage(Uri imageUri, final int position) {

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd_HHmmss" );
        String currentDateAndTime = sdf.format( new Date( ) );


        final String fileName = getFileName( imageUri );

        File file = new File( FileUtil.getPath( imageUri, this ) );

        //    final StorageReference fileToUpload = mStorage.child( "ShopProduct" ).child( currentDateAndTime + "_" + fileName );

        RequestBody requestBody = RequestBody.create( MediaType.parse( "image/*" ), file );



        Bitmap thumb_bitmap = new Compressor( this )
                .setQuality( 75 )
                .compressToBitmap( file );

        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        thumb_bitmap.compress( Bitmap.CompressFormat.JPEG, 100, baos );





        final MultipartBody.Part body = MultipartBody.Part.createFormData( "uploaded_file", file.getName(), requestBody );

        new Thread( new Runnable( ) {
            @Override
            public void run() {


                // edit with location

                uploaderApi.uploadFile( body  ).enqueue( new Callback<String>( ) {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {




                        // remove from temp list



                        Toast.makeText( ProductDetails.this, response.code() +"   " + response.body(), Toast.LENGTH_SHORT ).show( );

                        fileNameList.set( position, true );
                        uploadListAdapter.notifyDataSetChanged( );

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                        Toast.makeText( ProductDetails.this, t.getMessage( ), Toast.LENGTH_SHORT ).show( );

                    }
                } );

            }
        } ).start( );


        uploadListAdapter.notifyDataSetChanged( );
    }















}
