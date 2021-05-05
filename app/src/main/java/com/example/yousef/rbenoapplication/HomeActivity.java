package com.example.yousef.rbenoapplication;import android.content.ComponentName;import android.content.Intent;import android.content.pm.PackageManager;import android.graphics.Color;import android.graphics.PorterDuff;import android.net.Uri;import android.os.Build;import android.os.Bundle;import android.util.Log;import android.view.MenuItem;import android.view.View;import android.widget.FrameLayout;import android.widget.ImageView;import android.widget.TextView;import android.widget.Toast;import androidx.annotation.NonNull;import androidx.annotation.Nullable;import androidx.appcompat.app.AppCompatActivity;import androidx.appcompat.widget.Toolbar;import androidx.core.app.NotificationManagerCompat;import androidx.core.content.ContextCompat;import androidx.core.view.GravityCompat;import androidx.drawerlayout.widget.DrawerLayout;import androidx.fragment.app.Fragment;import androidx.fragment.app.FragmentManager;import androidx.fragment.app.FragmentTransaction;import androidx.viewpager2.widget.ViewPager2;import com.facebook.AccessToken;import com.facebook.login.LoginManager;import com.google.android.gms.ads.MobileAds;import com.google.android.gms.tasks.OnSuccessListener;import com.google.android.material.badge.BadgeDrawable;import com.google.android.material.bottomnavigation.BottomNavigationView;import com.google.android.material.bottomsheet.BottomSheetDialog;import com.google.android.material.navigation.NavigationView;import com.google.android.material.tabs.TabLayout;import com.google.android.material.tabs.TabLayoutMediator;import com.google.firebase.auth.FirebaseAuth;import com.google.firebase.auth.FirebaseUser;import com.google.firebase.database.ChildEventListener;import com.google.firebase.database.DataSnapshot;import com.google.firebase.database.DatabaseError;import com.google.firebase.database.DatabaseReference;import com.google.firebase.database.FirebaseDatabase;import com.google.firebase.firestore.CollectionReference;import com.google.firebase.firestore.DocumentChange;import com.google.firebase.firestore.DocumentSnapshot;import com.google.firebase.firestore.EventListener;import com.google.firebase.firestore.FirebaseFirestore;import com.google.firebase.firestore.FirebaseFirestoreException;import com.google.firebase.firestore.ListenerRegistration;import com.google.firebase.firestore.Query;import com.google.firebase.firestore.QuerySnapshot;import com.squareup.picasso.Picasso;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.Objects;public class HomeActivity extends AppCompatActivity {  private final FirebaseAuth auth = FirebaseAuth.getInstance();  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();  public DocumentSnapshot documentSnapshot;  public ViewPager2 viewPager;  NewestPromosFragment newestPromosFragment;  private DrawerLayout drawerLayout;  private NavigationView navigation;  private BottomNavigationView bottomNavigationView;  private FrameLayout homeContainer, homeDialogFragmentContainer;  private final FirebaseUser user = auth.getCurrentUser();  private int messagesCount = 0;  private List<String> savedMessagesNotifications;  private Map<DatabaseReference, ChildEventListener> childEventListeners;  private final List<ListenerRegistration> eventListeners = new ArrayList<>();  private TabLayout tabLayout;  @Override  protected void onCreate(Bundle savedInstanceState) {    super.onCreate(savedInstanceState);    setContentView(R.layout.activity_home);//        final CollectionReference usersRef =//            FirebaseFirestore.getInstance().collection("users");////    FirebaseFirestore.getInstance().collection("promotions").get()//            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {//              @Override//              public void onSuccess(QuerySnapshot snapshots) {////                for(DocumentSnapshot promoSnapshot:snapshots){////                  String promoUid = promoSnapshot.getString("uid");////                  if(promoUid == null)//                    continue;////                  usersRef.document(promoUid)//                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {//                    @Override//                    public void onSuccess(DocumentSnapshot userSnapshot) {////                      String countryCode = userSnapshot.getString("countryCode");////                      if(countryCode != null){////                        promoSnapshot.getReference().update(//                                "country",countryCode.toUpperCase(),//                                "countryCode", FieldValue.delete());//                      }//////                    }//                  });//                }////              }//            });////    usersRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {//      @Override//      public void onSuccess(QuerySnapshot snapshots) {//        for(DocumentSnapshot snapshot:snapshots.getDocuments()){//          final String userId = snapshot.getString("userId");////          if(!userId.equals(snapshot.getId())){//            snapshot.getReference().delete();//          }//        }//      }//    });//    FirebaseFirestore.getInstance().collection("users")//            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {//      @Override//      public void onSuccess(QuerySnapshot snapshots) {////        for(DocumentSnapshot snapshot:snapshots.getDocuments()){////          if(snapshot.contains("countryCode") && snapshot.getString("countryCode")!=null)//            continue;////          final String country = snapshot.getString("country");////          String countryCode;////          if(country!=null){//            countryCode = getCountryCode(country,"en");////            if(countryCode == null){//              countryCode = getCountryCode(country,"ar");//            }////            if(countryCode!=null){//            snapshot.getReference().update("countryCode",countryCode);//          }//          }//        }//      }//    });    startService(new Intent(this, ShutdownService.class));    viewPager = findViewById(R.id.viewPager);    navigation = findViewById(R.id.homenavigation);    tabLayout = findViewById(R.id.tabLayout);    bottomNavigationView = findViewById(R.id.bottomNavigationView);    homeContainer = findViewById(R.id.homeFragmentContainer);    homeDialogFragmentContainer = findViewById(R.id.homeDialogFragmentContainer);    drawerLayout = findViewById(R.id.homedrawer);    Toolbar toolbar = findViewById(R.id.toolbar1);    toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);    toolbar.inflateMenu(R.menu.home_toolbar);    toolbar.setNavigationOnClickListener(new View.OnClickListener() {      @Override      public void onClick(View view) {        if (user.isAnonymous()) {          showSigninDialog();        } else {          showDrawer();        }      }    });    MobileAds.initialize(getApplicationContext());    bottomNavigationView.setSelectedItemId(R.id.homeNavigationItem);    if (user != null && !user.isAnonymous()) {      navigation.setNavigationItemSelectedListener(menuItem -> {        drawerLayout.closeDrawer(GravityCompat.START);        if (menuItem.getItemId() == R.id.my_ads) {//            removeAllPreviousFragments();          popAllBackstacks();          addFragmentToHomeContainer(new MyPromotionsFragment());        } else if (menuItem.getItemId() == R.id.my_settings) {//            removeAllPreviousFragments();          popAllBackstacks();          addFragmentToHomeContainer(new SettingsFragment());        } else if (menuItem.getItemId() == R.id.my_fav) {//          removeAllPreviousFragments();////            for(int i=0;i<getSupportFragmentManager().getBackStackEntryCount();i++){////              getSupportFragmentManager().popBackStack();//              getSupportFragmentManager().getBackStackEntryAt(i).//            }          popAllBackstacks();          addFragmentToHomeContainer(new FavouriteFragment());        } else if (menuItem.getItemId() == R.id.my_guide) {//            removeAllPreviousFragments();        } else if (menuItem.getItemId() == R.id.add_Promo) {          showPromoOptions();        }        return false;      });      firestore.collection("users")              .whereEqualTo("userId", user.getUid())              .get().addOnSuccessListener(queryDocumentSnapshots -> {        documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);        eventListeners.add(                documentSnapshot.getReference().addSnapshotListener((value, error) -> {                  //            GlobalVariables.setBlockedUsers((List<String>) value.get("usersBlocked"));                  GlobalVariables.setFavPromosIds((List<Long>) value.get("favpromosids"));                  Log.d("ttt", "fav promos updated");                }));        final String userName = documentSnapshot.getString("username");        final String imageUrl = documentSnapshot.getString("imageurl");//          GlobalVariables.getInstance().getCountryCode();////          GlobalVariables.setCurrency(documentSnapshot.getString("currency"));//          GlobalVariables.setCountry(documentSnapshot.getString("country"));//          String code = documentSnapshot.getString("countryCode");//          GlobalVariables.setCountryCode(code);        GlobalVariables.setCurrentToken(documentSnapshot.getString("token"));//          ((TextView) findViewById(R.id.toolbarTitleTv)).setText("Rbeno "+//                  EmojiUtil.countryCodeToEmoji(GlobalVariables.getInstance().getCountryCode()));        initializeTABS();        ((TextView) navigation.getHeaderView(0)                .findViewById(R.id.username)).setText(userName);        ((TextView) navigation.getHeaderView(0)                .findViewById(R.id.staticusername)).setText("@" + userName.toLowerCase().trim());        if (documentSnapshot.getBoolean("status")) {          ((ImageView) navigation.getHeaderView(0)                  .findViewById(R.id.statusImageView))                  .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),                          R.drawable.green_circle));        } else {          ((ImageView) navigation.getHeaderView(0)                  .findViewById(R.id.statusImageView))                  .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),                          R.drawable.red_circle));        }        if (imageUrl != null && !imageUrl.isEmpty()) {          Picasso.get().load(imageUrl).fit().centerCrop().into((ImageView)                  navigation.getHeaderView(0).findViewById(R.id.profile_image));        }      });      findViewById(R.id.addPromotionBtn2).setOnClickListener(v -> showPromoOptions());      findViewById(R.id.nav_footer_Button).setOnClickListener(v -> {        if (WifiUtil.checkWifiConnection(this)) {          if (AccessToken.getCurrentAccessToken() != null) {            LoginManager.getInstance().logOut();          }          NotificationManagerCompat.from(this).cancelAll();          auth.signOut();          getPackageManager().setComponentEnabledSetting(                  new ComponentName(HomeActivity.this, MyFirebaseMessaging.class),                  PackageManager.COMPONENT_ENABLED_STATE_DISABLED,                  PackageManager.DONT_KILL_APP);          Toast.makeText(HomeActivity.this, "تم تسجيل الخروج!",                  Toast.LENGTH_SHORT).show();          startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));          finish();        }      });      bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {        if (menuItem.getItemId() == R.id.homeNavigationItem) {          menuItem.setCheckable(true);          if (homeContainer.getVisibility() == View.VISIBLE) {            homeContainer.setVisibility(View.INVISIBLE);            getSupportFragmentManager().beginTransaction()                    .remove(getSupportFragmentManager().getFragments().get(                            getSupportFragmentManager().getFragments().size() - 1                    )).commit();          }        } else if (menuItem.getItemId() == R.id.userNavigationItem) {          if (bottomNavigationView.getSelectedItemId() != R.id.userNavigationItem) {            menuItem.setCheckable(true);            homeContainer.setVisibility(View.VISIBLE);            getSupportFragmentManager().beginTransaction()                    .replace(R.id.homeFragmentContainer, new UserFragment(), "userFragment")                    .commit();          }        } else if (menuItem.getItemId() == R.id.notificationNavigationItem) {          if (bottomNavigationView.getSelectedItemId() != R.id.notificationNavigationItem) {            menuItem.setCheckable(true);            homeContainer.setVisibility(View.VISIBLE);            getSupportFragmentManager().beginTransaction().replace(R.id.homeFragmentContainer,                    new NotificationsFragment(), "notification").commit();          }        } else if (menuItem.getItemId() == R.id.add_promo_tv) {          showPromoOptions();          return false;        } else if (menuItem.getItemId() == R.id.messagesNavigationItem) {          if (bottomNavigationView.getSelectedItemId() == R.id.messagesNavigationItem) {            ((MessagesFragment) getSupportFragmentManager().findFragmentByTag("messages"))                    .resetToFirstFragment();          } else {            menuItem.setCheckable(true);            homeContainer.setVisibility(View.VISIBLE);            if (getSupportFragmentManager().findFragmentByTag("messages") != null) {              getSupportFragmentManager().beginTransaction().remove(                      getSupportFragmentManager().findFragmentByTag("messages")).commit();            }            getSupportFragmentManager().beginTransaction().replace(R.id.homeFragmentContainer,                    new MessagesFragment(), "messages").commit();          }        }        return true;      });      setNotificationBadgeNew();      savedMessagesNotifications = new ArrayList<>();      childEventListeners = new HashMap<>();      addMessagesNotificationListener("sender");      addMessagesNotificationListener("receiver");    } else {//        ((TextView) findViewById(R.id.toolbarTitleTv)).setText("Rbeno "+//                EmojiUtil.countryCodeToEmoji(GlobalVariables.getInstance().getCountryCode()));      drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);      initializeTABS();      findViewById(R.id.addPromotionBtn2).setOnClickListener(v -> showSigninDialog());      findViewById(R.id.nav_footer_Button).setOnClickListener(v -> {        startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));      });      bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {        if (menuItem.getItemId() == R.id.homeNavigationItem) {          menuItem.setCheckable(true);          if (homeContainer.getVisibility() == View.VISIBLE) {            homeContainer.setVisibility(View.INVISIBLE);            getSupportFragmentManager().beginTransaction().remove(                    getSupportFragmentManager().getFragments().get(                            getSupportFragmentManager().getFragments().size() - 1                    )).commit();          }        } else {          showSigninDialog();        }        return false;      });    }    createPromotionDeleteChangeReceiver("isDeleted");    createPromotionDeleteChangeReceiver("isPaused");    createPromotionDeleteChangeReceiver("isBanned");  }  void createPromotionDeleteChangeReceiver(String field) {    final Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".promoDelete");    eventListeners.add(            FirebaseFirestore.getInstance().collection("promotions")                    .whereEqualTo(field, true)                    .addSnapshotListener(new EventListener<QuerySnapshot>() {                      @Override                      public void onEvent(@Nullable QuerySnapshot value,                                          @Nullable FirebaseFirestoreException error) {                        if (!field.equals("isPaused")) {                          for (DocumentChange dc : value.getDocumentChanges()) {                            if (dc.getType() == DocumentChange.Type.ADDED) {                              intent.putExtra("promoId",                                      dc.getDocument().getLong("promoid"))                                      .putExtra("changeType", field);                              sendBroadcast(intent);                            }                          }                        } else {                          if (value != null) {                            for (DocumentChange dc : value.getDocumentChanges()) {                              if (dc.getType() == DocumentChange.Type.ADDED) {                                Log.d("ttt", "added to paused");                                intent.putExtra("promoId",                                        dc.getDocument().getLong("promoid"))                                        .putExtra("changeType", field);                                sendBroadcast(intent);                              } else if (dc.getType() == DocumentChange.Type.REMOVED) {                                Log.d("ttt", "removed from paused");                                intent.putExtra("promoId",                                        dc.getDocument().getLong("promoid"))                                        .putExtra("changeType", "isResumed");                                sendBroadcast(intent);                              }                            }                          }                        }                      }                    }));  }  @Override  public void onBackPressed() {    Log.d("ttt", "back clicked");    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {      Log.d("ttt", "drawer is open man ??");      drawerLayout.closeDrawer(GravityCompat.START);    } else {      if (homeDialogFragmentContainer.getVisibility() == View.VISIBLE) {        getSupportFragmentManager().popBackStack();        Log.d("ttt", "home dialog fragment count: " +                getSupportFragmentManager().getBackStackEntryCount());        Log.d("ttt", "fragments size: " + getSupportFragmentManager().getFragments().size());        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {          homeDialogFragmentContainer.setVisibility(View.GONE);        }      } else if (homeContainer.getVisibility() == View.VISIBLE) {        Log.d("ttt", "home container visible");        bottomNavigationView.setSelectedItemId(R.id.homeNavigationItem);      } else {        Log.d("ttt", "bundles empty");        if (tabLayout.getSelectedTabPosition() != 0) {          tabLayout.selectTab(tabLayout.getTabAt(0));        } else {          super.onBackPressed();        }      }    }  }  public void addFragmentToHomeContainer(Fragment fragment) {    homeDialogFragmentContainer.setVisibility(View.VISIBLE);    Log.d("ttt", "back stack: " + getSupportFragmentManager().getBackStackEntryCount());    final FragmentTransaction fragmentTransaction = getSupportFragmentManager()            .beginTransaction()            .add(R.id.homeDialogFragmentContainer, fragment)            .addToBackStack(null);    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {      fragmentTransaction              .hide(getSupportFragmentManager().getFragments()                      .get(getSupportFragmentManager().getFragments().size() - 1));    }    fragmentTransaction.commit();  }  void showSigninDialog() {    SigninUtil.getInstance(HomeActivity.this, HomeActivity.this).show();  }  void showPromoOptions() {    final BottomSheetDialog bsd = new BottomSheetDialog(HomeActivity.this);    final View parentView = getLayoutInflater().inflate(R.layout.bottomsheetmenu, null);    parentView.findViewById(R.id.imagePromoBtn).setOnClickListener(v ->            checkPromotionLimitAndStartActivity(2, "image", bsd));    parentView.findViewById(R.id.videoPromoBtn).setOnClickListener(v ->            checkPromotionLimitAndStartActivity(1, "video", bsd));    parentView.findViewById(R.id.textPromoBtn).setOnClickListener(v ->            checkPromotionLimitAndStartActivity(3, "text", bsd));    parentView.findViewById(R.id.closePromoIv).setOnClickListener(view -> bsd.dismiss());    bsd.setContentView(parentView);    bsd.show();  }  void setNotificationBadgeNew() {    final CollectionReference notificationsRef =            firestore.collection("notifications");    final Query notificationQuery = notificationsRef.whereEqualTo("receiverId", user.getUid());    eventListeners.add(            notificationQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {              int count = 0;              @Override              public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {                if (snapshots == null)                  return;                for (DocumentChange dc : snapshots.getDocumentChanges()) {                  switch (dc.getType()) {                    case ADDED:                      count++;                      break;                    case REMOVED:                      count--;                      break;                  }                  bottomNavigationView.getOrCreateBadge(R.id.notificationNavigationItem)                          .setVisible(count != 0);                }              }            }));  }  void addMessagesNotificationListener(String type) {    ChildEventListener childEventListener;    final com.google.firebase.database.Query query =            FirebaseDatabase.getInstance().getReference()                    .child("Messages")                    .orderByChild(type)                    .equalTo(user.getUid());    query.addChildEventListener(            childEventListener = new ChildEventListener() {              @Override              public void onChildAdded(@NonNull DataSnapshot snapshot,                                       @Nullable String previousChildName) {                Log.d("messageNotifs", "onChildAdded: ");                calculateUnreadCountNew(snapshot);              }              @Override              public void onChildChanged(@NonNull DataSnapshot snapshot,                                         @Nullable String previousChildName) {                Log.d("messageNotifs", "onChildChanged: ");                if (snapshot.child("isDeletedFor:" + user.getUid()).getValue(Boolean.class)) {                  if (savedMessagesNotifications.contains(snapshot.getKey()))                    messagesCount--;                  updateMessageBadgeCount();                } else {                  calculateUnreadCountNew(snapshot);                }              }              @Override              public void onChildRemoved(@NonNull DataSnapshot snapshot) {              }              @Override              public void onChildMoved(@NonNull DataSnapshot snapshot,                                       @Nullable String previousChildName) {              }              @Override              public void onCancelled(@NonNull DatabaseError error) {              }            });    childEventListeners.put(query.getRef(), childEventListener);  }  void updateMessageBadgeCount() {    final BadgeDrawable badge =            bottomNavigationView.getOrCreateBadge(R.id.messagesNavigationItem);    if (messagesCount == 0) {      badge.setVisible(false);    } else {      badge.setNumber(messagesCount);      badge.setVisible(true);    }  }  void calculateUnreadCountNew(DataSnapshot snapshot) {    long unreadCount = snapshot.child("messages").getChildrenCount() -            snapshot.child(user.getUid() + ":LastSeenMessage")                    .getValue(Long.class);    Log.d("messageNotifs", "unreadCount: " + unreadCount);    if (unreadCount > 0) {      if (!savedMessagesNotifications.contains(snapshot.getKey())) {        savedMessagesNotifications.add(snapshot.getKey());        messagesCount++;      }    } else {      if (savedMessagesNotifications.contains(snapshot.getKey())) {        if (Build.VERSION.SDK_INT < 26) {          BadgeUtil.decrementBadgeNum(HomeActivity.this);        }        savedMessagesNotifications.remove(snapshot.getKey());        messagesCount--;      }    }    updateMessageBadgeCount();  }  public void changeStatusIcon(int drawableId) {    ((ImageView) navigation.getHeaderView(0).findViewById(R.id.statusImageView))            .setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), drawableId));  }  public void changeProfileImage(Uri uri) {    Picasso.get().load(uri).fit().centerCrop().into(            ((ImageView) navigation.getHeaderView(0).findViewById(R.id.profile_image))    );  }  public void showDrawer() {    drawerLayout.openDrawer(GravityCompat.START);  }  @Override  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {    super.onActivityResult(requestCode, resultCode, data);    if (resultCode == 3 && data != null) {      newestPromosFragment.getAddedPromo(data.getLongExtra("addedPromoId", 0));      if (getSupportFragmentManager().findFragmentByTag("myPromotionsFragment") != null) {        ((MyPromotionsFragment) getSupportFragmentManager()                .findFragmentByTag("myPromotionsFragment")).onRefresh();      }      if (getSupportFragmentManager().findFragmentByTag("userFragment") != null) {        ((UserFragment) getSupportFragmentManager().findFragmentByTag("userFragment")).onRefresh();      }    }  }  void initializeTABS() {    final String[] categories = {"الكل", "أثاث", "الكترونيات", "هواتف", "عقارات", "سيارات", "خدمات",            "حيوانات و طيور", "مستلزمات شخصية", "اخرى"};    final Integer[] icons = {R.drawable.home_icon, R.drawable.furniture_icon_grey,            R.drawable.pc_icon_grey, R.drawable.mobile_icon_grey, R.drawable.home_icon_grey,            R.drawable.car_icon_grey, R.drawable.services_icon, R.drawable.bird_icon,            R.drawable.personal_items_icon, R.drawable.shopping_cart_icon};    final List<Fragment> fragmentList = new ArrayList<>();    fragmentList.add(newestPromosFragment = new NewestPromosFragment());    fragmentList.add(new CategoryPromotionsFragment(categories[1]));    fragmentList.add(new CategoryPromotionsFragment(            new String[]{categories[2], "كمبيوتر و لاب توب"}));    fragmentList.add(new CategoryPromotionsFragment(categories[3]));    fragmentList.add(new CategoryPromotionsFragment(categories[4]));    fragmentList.add(new CategoryPromotionsFragment(categories[5]));    fragmentList.add(new CategoryPromotionsFragment(categories[6]));    fragmentList.add(new CategoryPromotionsFragment(categories[7]));    fragmentList.add(new CategoryPromotionsFragment(categories[8]));    fragmentList.add(new CategoryPromotionsFragment(categories[9]));    viewPager.setAdapter(new HomeStateTabAdapter(this, fragmentList));    new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {      tab.setCustomView(R.layout.custom_home_tab_layout);      final View customView = tab.getCustomView();      ((TextView) customView.findViewById(R.id.customTabTv)).setText(categories[position]);      ((ImageView) customView.findViewById(R.id.customTabIv)).setImageResource(icons[position]);    }).attach();    selectOrUnSelectTab(true, tabLayout.getTabAt(0));    viewPager.setOffscreenPageLimit(1);    final List<Integer> selectedPositions = new ArrayList<>();    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {      @Override      public void onTabSelected(TabLayout.Tab tab) {        if (!selectedPositions.contains(tab.getPosition())) {          viewPager.setOffscreenPageLimit(viewPager.getOffscreenPageLimit() + 1);          selectedPositions.add(tab.getPosition());        }        selectOrUnSelectTab(true, tab);      }      @Override      public void onTabUnselected(TabLayout.Tab tab) {        selectOrUnSelectTab(false, tab);      }      @Override      public void onTabReselected(TabLayout.Tab tab) {      }    });  }  private void selectOrUnSelectTab(boolean select, TabLayout.Tab tab) {    final View customView = tab.getCustomView();    final TextView tabTv = customView.findViewById(R.id.customTabTv);    final ImageView tabIv = customView.findViewById(R.id.customTabIv);    if (select) {      tabTv.setTextColor(getResources().getColor(R.color.white));      int tvBackColor = Color.rgb(190, 21, 34);      tabTv.getBackground().setColorFilter(tvBackColor, PorterDuff.Mode.SRC_ATOP);      tabIv.getBackground().setColorFilter(tvBackColor, PorterDuff.Mode.SRC_ATOP);      tabIv.setColorFilter(Color.rgb(255, 255, 255), PorterDuff.Mode.SRC_ATOP);    } else {      tabTv.setTextColor(getResources().getColor(R.color.textGreyColor));      tabTv.getBackground().clearColorFilter();      tabIv.getBackground().clearColorFilter();      tabIv.clearColorFilter();    }  }  @Override  protected void onDestroy() {    if (childEventListeners != null && !childEventListeners.isEmpty()) {      for (DatabaseReference reference : childEventListeners.keySet()) {        reference.removeEventListener(Objects.requireNonNull(childEventListeners.get(reference)));      }    }    if (eventListeners != null && !eventListeners.isEmpty()) {      for (ListenerRegistration listener : eventListeners) {        listener.remove();      }    }    super.onDestroy();  }  private void popAllBackstacks() {    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);  }  @Override  public boolean onOptionsItemSelected(MenuItem item) {    if (item.getItemId() == android.R.id.home) {      if (drawerLayout.isDrawerOpen(GravityCompat.START)) {        drawerLayout.closeDrawer(GravityCompat.START);      } else {        drawerLayout.openDrawer(GravityCompat.START);      }    } else if (item.getItemId() == R.id.search_item) {      if (!user.isAnonymous()) {        Bundle b = new Bundle();        b.putString("userdocument", documentSnapshot.getId());        Fragment dialogFragment = SearchFragment.newInstance();        dialogFragment.setArguments(b);        addFragmentToHomeContainer(dialogFragment);      } else {        addFragmentToHomeContainer(SearchFragment.newInstance());      }    } else if (item.getItemId() == R.id.filter_item) {      addFragmentToHomeContainer(FilterFragment.newInstance());    }    return super.onOptionsItemSelected(item);  }  public void lockDrawer(int type) {    drawerLayout.setDrawerLockMode(type);  }  void checkPromotionLimitAndStartActivity(int limit, String type, BottomSheetDialog bsd) {    if (WifiUtil.checkWifiConnection(HomeActivity.this)) {      FirebaseFirestore.getInstance().collection("promotions")              .whereEqualTo("uid", user.getUid())              .whereEqualTo("promoType", type)              .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {        @Override        public void onSuccess(QuerySnapshot snapshots) {          if (snapshots.size() >= limit) {            String promoType = "";            switch (type) {              case "video":                promoType = "الفيديو";                break;              case "text":                promoType = "النصية";                break;              case "image":                promoType = "الصورة";                break;            }            Toast.makeText(HomeActivity.this,                    "لقد وصلت للحد الاقصى للنشر لاعلانات من نوع " + promoType,                    Toast.LENGTH_SHORT).show();          } else {            final Intent intent = new Intent(getApplicationContext(), PromotionActivity.class);            switch (type) {              case "video":                intent.putExtra("videoPromo", true);                break;              case "text":                intent.putExtra("textPromo", true);                break;            }            bsd.dismiss();            startActivityForResult(intent, 0);          }        }      });    }  }////  public String getCountryCode(String countryName, String language) {////    String[] isoCountryCodes = Locale.getISOCountries();//    Locale locale;//    String name;//////    for (String code : isoCountryCodes) {//      locale = new Locale(language, code);//      name = locale.getDisplayCountry(locale);////      if(name.equals(countryName)){//        return code;//      }//    }////    return null;//  }}