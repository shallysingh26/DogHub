//I have established a key here to identify areas of improvement
//they are marked with "IMP"


package com.example.doghub;

//importing all the required classes/packages

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import android.widget.*;

import java.util.*;

public class CreateProfile extends AppCompatActivity {


    //creating the variables
    EditText etName, etDogName, etDogBreed, etDogAge, etBio;
    Button button;
    ImageView imageView, imageView2;
    ProgressBar progessBar;
    Uri imageUri, imageUri2;
    UploadTask uploadTask;
    StorageReference storageReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    private static final int Pick_Image = 1;
    All_User_Members member;
    String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);


        //equating the variables declared above with their respective IDs
        member = new All_User_Members();
        imageView = findViewById(R.id.imageView_dog_cp);
        imageView2 = findViewById(R.id.imageView_cp);
        etName = findViewById(R.id.et_name_cp);
        etBio = findViewById(R.id.et_bio_cp);
        etDogName = findViewById(R.id.et_dog_name_cp);
        etDogBreed = findViewById(R.id.et_breed_cp);
        etDogAge = findViewById(R.id.et_dog_age_cp);
        button = findViewById(R.id.btn_cp);
        progessBar = findViewById(R.id.progressbar_cp);

        //getting the instance of the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        currentUserID = user.getUid();

        //declaring the references
        documentReference = db.collection("user").document(currentUserID);
        storageReference = FirebaseStorage.getInstance().getReference("Profile Images");
        databaseReference = database.getReference("All Users");

        //declaring what the button will do once clicked
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            //on clicking, the upload data method will be called
            public void onClick(View v) {
                uploadData();
            }
        });

        //once user clicks on the image, it will allow them to select an image from their phone
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //setting type of variable to any type of image, not limiting to just one for example jpeg
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, Pick_Image);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == Pick_Image || resultCode == RESULT_OK || data != null || data.getData() != null) {

                imageUri = data.getData();

                //loading an image into Picasso
                Picasso.get().load(imageUri).into(imageView);

            }

        } catch (Exception e) {
            Toast.makeText(this, "Error" + e, Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExt(Uri uri) {

        //we are using a two-way map that maps MIME-types to file extensions and vice versa
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType((contentResolver.getType(uri)));
    }

    private void uploadData() {

        //converting all entered data into Strings for now, including age
        //will have to change later so that age only takes in number
        //this comes in the improvements step of the code (IMP)


        String name = etName.getText().toString();
        String dogs_name = etDogName.getText().toString();
        String bio = etBio.getText().toString();
        String breed = etDogBreed.getText().toString();
        String dogs_age = etDogAge.getText().toString();


        //making sure that all fields aren't blank
        if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(bio) || !TextUtils.isEmpty(dogs_name) || !TextUtils.isEmpty(breed) || !TextUtils.isEmpty(dogs_age) || imageUri != null) {


            //setting the progress bar visibility on while the data is being loaded into firebase
            //might have to work on this(IMP)

            progessBar.setVisibility(View.VISIBLE);
            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
            uploadTask = reference.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask((new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            })).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Map<String, String> profile = new HashMap<>();
                        profile.put("name", name);
                        profile.put("dogs_name", dogs_name);
                        profile.put("breed", breed);
                        profile.put("bio", bio);
                        profile.put("dogs_age", dogs_age);
                        profile.put("url", downloadUri.toString());
                        profile.put("privacy", "Public");

//saving data in the realtime database
                        member.setName(name);
                        member.setBio(bio);
                        member.setBreed(breed);
                        member.setDogs_age(dogs_age);
                        member.setDogs_name(dogs_name);
                        member.setUid(currentUserID);
                        member.setUrl(downloadUri.toString());

                        databaseReference.child(currentUserID).setValue(member);

                        documentReference.set(profile).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                //adding progress bar and displaying a toast message for a successful profile creation

                                //progressBar.setVisiblity(View.INVISIBLE);
                                Toast.makeText(CreateProfile.this, "Profile Created", Toast.LENGTH_SHORT).show();

                                //using Handler to send user after some time to fragment 1 after 2 seconds after completion
                                //waiting for 2 seconds to avoid errors
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(CreateProfile.this, Fragment1.class);
                                        startActivity(intent);
                                    }
                                }, 2000);


                            }
                        });


                    }

                }
            });
        } else {
            //error toast message if all the fields are not filled
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }

    }

}