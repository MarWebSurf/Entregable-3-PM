package com.example.maria.entregable3potettimarianoandroid.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.maria.entregable3potettimarianoandroid.R;
import com.example.maria.entregable3potettimarianoandroid.controller.ControllerArtistaDatabase;
import com.example.maria.entregable3potettimarianoandroid.model.POJO.Artista;
import com.example.maria.entregable3potettimarianoandroid.model.POJO.Obra;
import com.example.maria.entregable3potettimarianoandroid.model.dao.Respuesta;
import com.example.maria.entregable3potettimarianoandroid.utils.ResultListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class DetalleActivity extends AppCompatActivity {

    public static final String CLAVE_OBRA = "clave obra";

    private TextView textViewDetalleNombreObra;
    private ImageView imageViewDetalle;
    private TextView textViewDetalleNombreArtista;
    private TextView textViewDetalleNacionalidad;
    private TextView textViewDetalleInfluenced_By;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;

    private Obra obraRecibidaPorBundleDesdeRecyclerMainActivity;
    private ControllerArtistaDatabase controllerArtistaDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Registrate para ver siempre todos los cuadros en detalle!", Toast.LENGTH_SHORT).show();
        }

        database = FirebaseDatabase.getInstance();

        textViewDetalleNombreObra = findViewById(R.id.textView_detalle_nombre_obra);
        imageViewDetalle = findViewById(R.id.imageView_Detalle);
        textViewDetalleNombreArtista = findViewById(R.id.textView_detalle_nombreArtista);
        textViewDetalleNacionalidad = findViewById(R.id.textView_detalle_Nacionalidad);
        textViewDetalleInfluenced_By = findViewById(R.id.textView_detalle_Influenced_By);

        Intent intentRecibido = getIntent();
        Bundle bundleRecibido = intentRecibido.getExtras();
        obraRecibidaPorBundleDesdeRecyclerMainActivity = (Obra) bundleRecibido.getSerializable(CLAVE_OBRA);
        textViewDetalleNombreObra.setText(obraRecibidaPorBundleDesdeRecyclerMainActivity.getName());

        controllerArtistaDatabase = new ControllerArtistaDatabase(this);
        traerArtistaPorIdDataBase(obraRecibidaPorBundleDesdeRecyclerMainActivity.getArtistId());
        traerImagenCuadroElegido(obraRecibidaPorBundleDesdeRecyclerMainActivity.getRutaImagen());


    }

    private void traerImagenCuadroElegido(String rutaImagen) {
        if (TextUtils.isEmpty(rutaImagen)) {
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference();
        reference = reference.child(rutaImagen);
        //opcion 1

        try {
            final File archivo = File.createTempFile("fotoandroid", "jpg");
            final StorageReference finalReference = reference;
            reference.getFile(archivo).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Glide.with(DetalleActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(finalReference)
                            .placeholder(R.drawable.placeholder16_9)
                            .error(R.drawable.error)
                            .into(imageViewDetalle);
                    // Picasso.get().load(archivo.getAbsoluteFile()).into(imagenContacto);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // imageViewDetalle.setImageResource(R.drawable.error);
                    //iba a dejar la linea de arriba pero me di cuenta que usando el glide
                    // si ya vi alguna vez las pinturas persisten en cache y las muestra con glide aun sin loguearse..

                    imageViewDetalle.setImageResource(R.drawable.error);
                    Glide.with(DetalleActivity.this)
                            .using(new FirebaseImageLoader())
                            .load(finalReference)
                            .placeholder(R.drawable.placeholder16_9)
                            .error(R.drawable.error)
                            .into(imageViewDetalle);
                }
            });
        } catch (Exception e) {

        }
    }


    public void traerArtistaPorIdDataBase(final String artistIdObraRecibidaPorBundle) {

        controllerArtistaDatabase.obtenerDatosArtistaDatabase(artistIdObraRecibidaPorBundle, new ResultListener<Respuesta>() {
            @Override
            public void finish(Respuesta resultado) {
                if (resultado.getArtista() != null) {
                    setearArtista(resultado.getArtista());

                } else {
                    Toast.makeText(DetalleActivity.this, resultado.getError(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void setearArtista(Artista artista) {
        textViewDetalleNombreArtista.setText(artista.getName());
        textViewDetalleNacionalidad.setText(artista.getNationality());
        textViewDetalleInfluenced_By.setText(artista.getInfluenced_by());
    }


}
