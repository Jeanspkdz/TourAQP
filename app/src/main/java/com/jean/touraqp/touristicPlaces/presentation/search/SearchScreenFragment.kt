package com.jean.touraqp.touristicPlaces.presentation.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.ImageLoader
import com.jean.touraqp.R
import com.jean.touraqp.core.UserSession
import com.jean.touraqp.databinding.FragmentSearchScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchScreenFragment : Fragment(R.layout.fragment_search_screen) {

    private val searchViewModel: SearchViewModel by viewModels()
    private var fragmentSearchScreenBinding: FragmentSearchScreenBinding? = null
    private lateinit var searchListAdapter: SearchListAdapter
    @Inject lateinit var userSession: UserSession

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSearchScreenBinding = FragmentSearchScreenBinding.bind(view)
        initUI()
        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentSearchScreenBinding = null
    }

    private fun initUI() {
        setUserInfo()
        setSearchListAdapter()
        setTouristicPlacesRecyclerView()
    }

    private fun setUserInfo() {
        fragmentSearchScreenBinding?.apply {
            topAppBar.title = "Hola ${userSession.name}"
            topAppBar.subtitle = "@${userSession.username}"
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchViewModel.searchState.collect() { state ->
                    fragmentSearchScreenBinding?.progressBar?.isVisible = state.isLoading
                    searchListAdapter.updateList(state.touristicPlaceList)
                }
            }
        }
    }

    private fun setSearchListAdapter() {
        searchListAdapter = SearchListAdapter(onClickListener = { id ->
            navigateToDetailTouristicPlace(id)
        })
    }

    private fun navigateToDetailTouristicPlace(id: String) {
        findNavController().navigate(
            SearchScreenFragmentDirections.actionSearchScreenFragmentToTouristicPlaceDetailScreenFragment(
                touristicPlaceId = id
            )
        )
    }

    private fun setTouristicPlacesRecyclerView() {
        fragmentSearchScreenBinding?.rvTouristicPlaces?.apply {
            setHasFixedSize(true)
            adapter = searchListAdapter
            layoutManager = LinearLayoutManager(this@SearchScreenFragment.context)
        }
    }
}