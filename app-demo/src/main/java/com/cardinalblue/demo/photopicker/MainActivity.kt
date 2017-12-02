package com.cardinalblue.demo.photopicker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.cardinalblue.demo.R
import com.cardinalblue.photopicker.*
import com.cardinalblue.photopicker.model.PhotoPickerStore
import com.cardinalblue.photopicker.view.GalleryPhotoPickerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainActivity : AppCompatActivity(),
    IPhotoPickerPresenterProvider,
    PhotoPickerToolbarContract.IToolbarView {

    // Views.
    private val mSelectionNum: TextView by lazy { findViewById<TextView>(R.id.selection_num) }

    private val mPhotoLoader: PhotoPickerContract.IPhotosLoader by lazy { GalleryPhotoLoader(contentResolver) }
    private val mSelectionStore: PhotoPickerStore by lazy { PhotoPickerStore(arrayListOf()) }

    // Logger.
    private val mLogger: PhotoPickerLogger by lazy { PhotoPickerLogger(this@MainActivity) }

    // Controllers/presenters.
    private val mPhotoPickerPresenter: PhotoPickerPresenter by lazy {
        PhotoPickerPresenter(mPhotoLoader,
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

        // Toolbar.
        mToolbarPresenter.bindViewOnCreate(this)

        // Photo picker.
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.picker, GalleryPhotoPickerFragment() as Fragment)
                .commit()
        }
    }

    override fun onDestroy() {
        // Toolbar.
        mToolbarPresenter.unBindViewOnDestroy()

        super.onDestroy()
    }

    override fun onResume() {
        // Toolbar.
        mToolbarPresenter.onResume()

        super.onResume()
    }

    override fun onPause() {
        // Toolbar.
        mToolbarPresenter.onPause()

        super.onPause()
    }

    override fun getPhotoPickerPresenter(): IPresenter<PhotoPickerContract.IPhotoPickerView> {
        return mPhotoPickerPresenter
    }

    override fun getToolbarPresenter(): IPresenter<PhotoPickerToolbarContract.IToolbarView> {
        return mToolbarPresenter
    }

    override fun setToolbarTitle(title: String?) {
        // DUMMY IMPL
    }

    override fun setSelectionCount(num: Int) {
        mSelectionNum.text = String.format(Locale.ENGLISH, "(%d)", num)
    }

    override fun onClickBackButton(): Observable<Any> {
        return Observable.just(0)
    }

    override fun onClickDoneButton(): Observable<Any> {
        return Observable.just(0)
    }
}
