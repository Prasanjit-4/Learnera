package com.semicolon.learnera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private OnPostClickListener mOnPostClickListener;
    private FirebaseFirestore firebaseFirestore;
    public Context context;
    public List<UserPost> course_list;
    public RecyclerViewAdapter(List<UserPost> course_list, OnPostClickListener onPostClickListener){
        this.course_list=course_list;
        this.mOnPostClickListener=onPostClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        context=parent.getContext();
        firebaseFirestore= FirebaseFirestore.getInstance();
        return new ViewHolder(view,mOnPostClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String webLink=course_list.get(position).getCourseLink();
        String postDescData = course_list.get(position).getDescription();
        holder.setPostDesc(postDescData);
        String image_url=course_list.get(position).getThumbPost();
        holder.setPost_image(image_url);
        String userID= course_list.get(position).getUserID();
        //retrieve user data
        firebaseFirestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    String username = task.getResult().getString("name");
                    String profileImage= task.getResult().getString("image");
                    holder.setUserData(username,profileImage);

                }

                else{
                    Toast.makeText(context,"error",Toast.LENGTH_SHORT).show();

                }
            }
        });
        long milliseconds=course_list.get(position).getPostTimeStamp().getTime();
        SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yy");
        String dateString= dateFormat.format(milliseconds);
        holder.setDate(dateString);



    }

    @Override
    public int getItemCount() {
        return course_list.size();
    }

    public class ViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {
        private View mView;
        private TextView postDesc,postDate;
        private ImageView post_image;
        private CircleImageView profileImage;
        private TextView userName;



        OnPostClickListener onPostClickListener;

        public ViewHolder(@NonNull View itemView, OnPostClickListener onPostClickListener) {
            super(itemView);
            mView=itemView;
            this.onPostClickListener=onPostClickListener;
            itemView.setOnClickListener(this);
        }

        // im typuinnfiomirandom text r8now cu z im recording a video

        // function to set description of post.
        public void setPostDesc(String text){
            postDesc=mView.findViewById(R.id.postDesc);
            postDesc.setText(text);

        }

        //function to set image to post by user
        public void setPost_image(String downloadUri){
            post_image=mView.findViewById(R.id.post_image);
            RequestOptions postPlaceHolder= new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
            postPlaceHolder.placeholder(R.drawable.post_image_place);

            Glide.with(context).applyDefaultRequestOptions(postPlaceHolder).load(downloadUri).into(post_image);

        }
        public void setDate(String date){
            postDate=mView.findViewById(R.id.postDate);
            postDate.setText(date);

        }
        public void setUserData(String name,String profile){
            userName=mView.findViewById(R.id.userName);
            profileImage= mView.findViewById(R.id.profileImage);

            userName.setText(name);
            RequestOptions placeHolderOption = new RequestOptions();
            placeHolderOption.placeholder(R.drawable.user_image_place);
            Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(profile).into(profileImage);

        }


        @Override
        public void onClick(View v) {
            onPostClickListener.onPostClick(getAdapterPosition());
        }


    }

    public interface OnPostClickListener{
        void onPostClick(int position);
    }
}
