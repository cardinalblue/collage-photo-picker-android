package com.cardinalblue.demo.photopicker.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.widget.TextView
import com.cardinalblue.demo.photopicker.MainContract
import com.cardinalblue.demo.photopicker.MainPresenter
import com.cardinalblue.demo.photopicker.R
import com.cardinalblue.photopicker.*
import com.cardinalblue.photopicker.model.PhotoPickerStore
import com.cardinalblue.photopicker.view.GalleryPhotoPickerFragment
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainActivity : AppCompatActivity(),
    IPhotoPickerPresenterProvider,
    PhotoPickerToolbarContract.IToolbarView,
    MainContract.IMainView {

    // Views.
    private val mSwitchSave: SwitchCompat by lazy { findViewById<SwitchCompat>(R.id.switch_save) }
    private val mSelectionNum: TextView by lazy { findViewById<TextView>(R.id.selection_num) }

    private val mPhotoLoader: PhotoPickerContract.IPhotosLoader by lazy { GalleryPhotoLoader(contentResolver) }
    private val mSelectionStore: PhotoPickerStore by lazy { PhotoPickerStore(arrayListOf()) }

    // Permissions helper.
    private val mPermissionsHelper: RxPermissions by lazy { RxPermissions(this@MainActivity) }

    // Logger.
    private val mLogger: PhotoPickerLogger by lazy { PhotoPickerLogger(this@MainActivity) }

    // Controllers/presenters.
    private val mMainPresenter: MainPresenter by lazy {
        MainPresenter(mPermissionsHelper,
                      getSharedPreferences(BuildConfig.APPLICATION_ID,
                                           Context.MODE_PRIVATE),
                      AndroidSchedulers.mainThread(),
                      Schedulers.io())
    }
    private val mPhotoPickerPresenter: PhotoPickerPresenter by lazy {
        PhotoPickerPresenter(mPermissionsHelper,
                             mPhotoLoader,
                             mSelectionStore,
                             AndroidSchedulers.mainThread(),
                             Schedulers.io(),
                             mLogger)
    }
    private val mToolbarPresenter: PhotoPickerToolbarPresenter by lazy {
        PhotoPickerToolbarPresenter(mSelectionStore,
                                    AndroidSchedulers.mainThread(),
                                    Schedulers.io(),
                                    mLogger)
    }

    // Observables.
    private val mDisposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Main presenter.
        mMainPresenter.bindViewOnCreate(this)

        // Toolbar presenter.
        mToolbarPresenter.bindViewOnCreate(this)

        // Photo picker presenter.
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.picker, GalleryPhotoPickerFragment() as Fragment)
                .commit()
        }
    }

    override fun onDestroy() {
        // Main presenter.
        mMainPresenter.unBindViewOnDestroy()

        // Toolbar.
        mToolbarPresenter.unBindViewOnDestroy()

        super.onDestroy()
    }

    override fun onResume() {
        // Main presenter.
        mMainPresenter.onResume()

        // Toolbar.
        mToolbarPresenter.onResume()

        super.onResume()
    }

    override fun onPause() {
        // Main presenter.
        mMainPresenter.onPause()

        // Toolbar.
        mToolbarPresenter.onPause()

        super.onPause()
    }

    /**
     * Expose the picker presenter for the fragment to bind view.
     */
    override fun getPhotoPickerPresenter(): IPresenter<PhotoPickerContract.IPhotoPickerView> {
        return mPhotoPickerPresenter
    }

    /**
     * Expose the toolbar presenter for the fragment to bind view.
     */
    override fun getToolbarPresenter(): IPresenter<PhotoPickerToolbarContract.IToolbarView> {
        return mToolbarPresenter
    }

    override fun setToolbarTitle(title: String?) {
        // DUMMY IMPL
    }

    override fun setSelectionCount(num: Int) {
        mSelectionNum.text = String.format(Locale.ENGLISH, "(%d)", num)
    }

    override fun getSwitchSaveChecked(): Boolean {
        return mSwitchSave.isChecked
    }

    override fun setSwitchSaveChecked(checked: Boolean) {
        mSwitchSave.isChecked = checked
    }

    override fun onClickBackButton(): Observable<Any> {
        return Observable.just(0)
    }

    override fun onClickDoneButton(): Observable<Any> {
        return Observable.just(0)
    }

    override fun onClickSwitchSave(): Observable<Boolean> {
        return RxCompoundButton
            .checkedChanges(mSwitchSave)
    }
}
