package com.cardinalblue.demo.photopicker

import io.reactivex.Observable

class MainContract {

    interface IMainView {

        fun getSwitchSaveChecked(): Boolean

        fun setSwitchSaveChecked(checked: Boolean)

        fun onClickSwitchSave(): Observable<Boolean>
    }
}
