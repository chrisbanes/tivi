package app.tivi.showdetails.episodedetails

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import app.tivi.util.TiviViewModel
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(

) : TiviViewModel() {

    var episodeId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                refresh()
            }
        }

    private val _data = MutableLiveData<EpisodeDetailsViewState>()
    val data: LiveData<EpisodeDetailsViewState>
        get() = _data

    private fun refresh() {
        val epId = episodeId
        if (epId != null) {

        } else {
            _data.value = null
        }
    }
}