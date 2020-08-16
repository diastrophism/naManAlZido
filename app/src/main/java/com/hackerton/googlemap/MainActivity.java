package com.hackerton.googlemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.hackerton.googlemap.Adapter.PageAdapter;
import com.hackerton.googlemap.fragment.CommunityMapfragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private TextView header_nameTextView;
    private TextView header_emailTextView;
    private ImageView header_photoImageView;
    private String profileString;

    private TextView nameTextView;
    private TextView emailTextView;

    private FirebaseAuth auth;
    private FirebaseUser user;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    CommunityMapfragment mapFragment;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private String displayName;


    @SuppressLint("ResourceType")
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this.getIntent());
        String sendingString = intent.getStringExtra("recent_review");
        String formatdate = intent.getStringExtra("recent_date");

        mTabLayout = (TabLayout) findViewById(R.id.main_tablayout);
        mTabLayout.addTab(mTabLayout.newTab().setText("Tab One"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Tab Two"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        // Initializing ViewPager
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);

        // Creating TabPagerAdapter adapter
        PageAdapter pagerAdapter = new PageAdapter(getSupportFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        // Set TabSelectedListener
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // 플롯팅 액션 버튼

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.xml.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.xml.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        //startActivity(intent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // 햄버거 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_foreground); //햄버거 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");

        NavigationView navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);

        // 네비게이션바 이름, 이메일 표시
        header_nameTextView = (TextView) view.findViewById(R.id.header_name_textView);
        header_emailTextView = (TextView) view.findViewById(R.id.header_email_textView);
        header_photoImageView = (ImageView) view.findViewById(R.id.header_photo_imageView);


        FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals("nickName")) {
                        displayName = snapshot.getValue(String.class);
                        header_nameTextView.setText(displayName);
                        Log.d("MainActivity", "success : " + displayName);
                    }
                    if(snapshot.getKey().equals("photoUrl")) {
                        profileString = snapshot.getValue(String.class);
                        Glide.with(context).load(profileString).into(header_photoImageView);
                        Log.d("MainActivity", snapshot.getKey() + " : " + profileString);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // 파이어베이스 이름 불러오기
        header_emailTextView.setText(user.getEmail());        // 파이어베이스 이메일 불러오

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if (id == R.id.account) {
                    Toast.makeText(context, title + ": 계정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                    if(auth != null){
                        Toast.makeText(context, " 정보 확인 ", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(MainActivity.this, MyPage.class);
                        startActivity(intent1);

                }
                } else if (id == R.id.setting) {
                    Toast.makeText(context, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.logout) {
                    Toast.makeText(context, title + ": 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    finish();
                    Intent intent3 = new Intent(MainActivity.this, LogInActivity.class);
                    startActivity(intent3);
                }
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // 햄버거 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);

                return true;
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
            ActivityCompat.finishAffinity(this);
        }
    }

    public void add_review(View view) {
        startActivity(new Intent(MainActivity.this, AddReview2.class));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                break;
            case R.id.fab1:
                anim();
                add_review(v);
                Toast.makeText(this, "Button1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab2:
                anim();
                //startActivity(new Intent(this, Content_Activity.class));
                Toast.makeText(this, "Button2", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void anim() {

        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }
}