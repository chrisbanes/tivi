package app.tivi.showdetails.episodedetails

import android.arch.lifecycle.MutableLiveData
import app.tivi.util.TiviViewModel
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(

) : TiviViewModel() {

    var episodeId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                if (value != null) {
                    // TODO
                } else {
                    data.value = null
                }
            }
        }

    val data = MutableLiveData<EpisodeDetailsViewState>()

    private fun refresh() {
        // TODO
    }
}