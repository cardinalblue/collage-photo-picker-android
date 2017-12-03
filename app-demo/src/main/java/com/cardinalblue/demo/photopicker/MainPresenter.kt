package com.cardinalblue.demo.photopicker

import android.Manifest
import android.content.SharedPreferences
import com.cardinalblue.photopicker.IPresenter
import com.cardinalblue.photopicker.model.PhotoPickerStore
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class MainPresenter(permissionsHelper: RxPermissions,
                    pickerStore: PhotoPickerStore,
                    preferences: SharedPreferences,
                    uiScheduler: Scheduler,
                    workerScheduler: Scheduler) :
    IPresenter<MainContract.IMainView> {

    companion object {
        const val KEY_SAVE: String = "key_save"
    }

    // Given.
    private val mPermissionsHelper: RxPermissions = permissionsHelper
    private val mPickerStore: PhotoPickerStore = pickerStore
    private val mPrefs: SharedPreferences = preferences
    private val mUiScheduler: Scheduler = uiScheduler
    private val mWorkScheduler: Scheduler = workerScheduler

    // Views.
    private var mMainView: MainContract.IMainView? = null

    // Permission.

    // Disposable
    private val mDisposablesOnCreate = CompositeDisposable()
    private val mDisposablesOnResume = CompositeDisposable()

    override fun bindViewOnCreate(view: MainContract.IMainView) {
        mMainView = view

        if (ifSaveSelection()) {
            // Enable the switch.
            mMainView?.setSwitchSaveChecked(true)

            // TODO: Read the selection from a file.
//            Observable
//                .fromCallable {
//                    // TODO: Complete it.
//                }
//                .subscribeOn(mWorkScheduler)
//                .observeOn(mUiScheduler)
//                .subscribe { selections ->
//                    // TODO: Complete it.
//                }
        }

        // Click on switch save.
        mDisposablesOnCreate.add(
            mMainView!!
                .onClickSwitchSave()
                .observeOn(mUiScheduler)
                .flatMap { checked: Boolean ->
                    if (checked) {
                        mPermissionsHelper
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        Observable.just(checked)
                    }
                }
                .subscribe { checked ->
                    mPrefs.edit()
                        .putBoolean(KEY_SAVE, checked)
                        .apply()

                    if (mMainView?.getSwitchSaveChecked() != checked) {
                        mMainView?.setSwitchSaveChecked(checked)
                    }
                })
    }

    override fun unBindViewOnDestroy() {
        mDisposablesOnCreate.clear()

        // Save the selection to a file.
        if (ifSaveSelection()) {

        }
    }

    override fun onResume() {
    }

    override fun onPause() {
        mDisposablesOnResume.clear()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private fun ifSaveSelection(): Boolean {
        return mPrefs.getBoolean(KEY_SAVE, false)
    }
}
