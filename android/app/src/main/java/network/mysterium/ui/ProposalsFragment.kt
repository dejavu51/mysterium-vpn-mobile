/*
 * Copyright (C) 2019 The "mysteriumnetwork/mysterium-vpn-mobile" Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package network.mysterium.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.proposal_list_item.*
import network.mysterium.AppContainer
import network.mysterium.MainApplication
import network.mysterium.ui.list.BaseItem
import network.mysterium.ui.list.BaseListAdapter
import network.mysterium.ui.list.BaseViewHolder
import network.mysterium.vpn.R

class ProposalsFragment : Fragment() {

    private lateinit var proposalsViewModel: ProposalsViewModel
    private lateinit var appContainer: AppContainer

    private lateinit var proposalsListRecyclerView: RecyclerView
    private lateinit var proposalsCloseButton: TextView
    private lateinit var proposalsSearchInput: EditText
    private lateinit var proposalsSwipeRefresh: SwipeRefreshLayout
    private lateinit var proposalsProgressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_proposals, container, false)

        appContainer = (activity!!.application as MainApplication).appContainer
        proposalsViewModel = appContainer.proposalsViewModel

        // Initialize UI elements.
        proposalsListRecyclerView = root.findViewById(R.id.proposals_list)
        proposalsCloseButton = root.findViewById(R.id.proposals_close_button)
        proposalsSearchInput = root.findViewById(R.id.proposals_search_input)
        proposalsSwipeRefresh = root.findViewById(R.id.proposals_list_swipe_refresh)
        proposalsProgressBar = root.findViewById(R.id.proposals_progress_bar)

        proposalsCloseButton.setOnClickListener { handleClose(root) }

        initProposalsList(root)
//        initProposalsSortDropdown(root)
//        initProposalsServiceTypeFilter(root)
        initProposalsSearchFilter()

        onBackPress {
            navigateTo(root, Screen.MAIN)
        }

        return root
    }

    private fun navigateToMainVpnFragment(root: View) {
        navigateTo(root, Screen.MAIN)
    }

    private fun initProposalsSearchFilter() {
        if (proposalsViewModel.filter.searchText != "") {
            proposalsSearchInput.setText(proposalsViewModel.filter.searchText)
        }

        proposalsSearchInput.onChange { proposalsViewModel.filterBySearchText(it) }
    }

//    private fun initProposalsServiceTypeFilter(root: View) {
//        // Set current active filter.
//        val activeTabButton = when (proposalsViewModel.filter.serviceType) {
//            ServiceTypeFilter.ALL -> proposalsFiltersAllButton
//            ServiceTypeFilter.FAVORITE -> proposalsFiltersFavoriteButton
//        }
//        setFilterTabActiveStyle(root, activeTabButton)
//
//        proposalsFiltersAllButton.setOnClickListener {
//            proposalsViewModel.filterByServiceType(ServiceTypeFilter.ALL)
//            setFilterTabActiveStyle(root, proposalsFiltersAllButton)
//            setFilterTabInactiveStyle(root, proposalsFiltersFavoriteButton)
//        }
//
//        proposalsFiltersFavoriteButton.setOnClickListener {
//            proposalsViewModel.filterByServiceType(ServiceTypeFilter.FAVORITE)
//            setFilterTabActiveStyle(root, proposalsFiltersFavoriteButton)
//            setFilterTabInactiveStyle(root, proposalsFiltersAllButton)
//        }
//    }


    private fun initProposalsList(root: View) {

        val listAdapter = BaseListAdapter{ selectedproposal ->
            val item = selectedproposal as ProposalItem?
            if (item != null) {
                handleSelectedProposal(root, item.uniqueId)
            }
        }
        proposalsListRecyclerView.adapter = listAdapter
        proposalsListRecyclerView.layoutManager = LinearLayoutManager(context)
        proposalsListRecyclerView.addItemDecoration(DividerItemDecoration(root.context, DividerItemDecoration.VERTICAL))
        proposalsSwipeRefresh.setOnRefreshListener {
            proposalsViewModel.refreshProposals {
                proposalsSwipeRefresh.isRefreshing = false
            }
        }

        // Subscribe to proposals changes.
        proposalsViewModel.getProposals().observe(this, Observer { newItems ->
            listAdapter.submitList(createProposalItemsWithGroups(newItems))
            listAdapter.notifyDataSetChanged()

            // Hide progress bar once proposals are loaded.
            proposalsListRecyclerView.visibility = View.VISIBLE
            proposalsProgressBar.visibility = View.GONE
        })


        proposalsViewModel.initialProposalsLoaded.observe(this, Observer {loaded ->
            if (loaded) {
                return@Observer
            }

            // If initial proposals failed to load during app init try to load them explicitly.
            proposalsListRecyclerView.visibility = View.GONE
            proposalsProgressBar.visibility = View.VISIBLE
            proposalsViewModel.refreshProposals {}
        })
    }

    private fun createProposalItemsWithGroups(groups: List<ProposalGroupViewItem>): MutableList<BaseItem> {
        val itemsWithHeaders = mutableListOf<BaseItem>()
        groups.forEach { group ->
            itemsWithHeaders.add(ProposalHeaderItem(group.title))
            group.children.forEach { proposal ->
                itemsWithHeaders.add(ProposalItem(proposal))
            }
        }
        return itemsWithHeaders
    }

    private fun handleClose(root: View) {
        hideKeyboard(root)
        navigateToMainVpnFragment(root)
    }

    private fun handleSelectedProposal(root: View, proposalID: String) {
        hideKeyboard(root)
        proposalsViewModel.selectProposal(proposalID)
        navigateToMainVpnFragment(root)
    }

    private fun setFilterTabActiveStyle(root: View, btn: TextView) {
        btn.setBackgroundColor(ContextCompat.getColor(root.context, R.color.ColorMain))
        btn.setTextColor(ContextCompat.getColor(root.context, R.color.ColorWhite))
    }

    private fun setFilterTabInactiveStyle(root: View, btn: TextView) {
        btn.setBackgroundColor(Color.TRANSPARENT)
        btn.setTextColor(ContextCompat.getColor(root.context, R.color.ColorMain))
    }
}

data class ProposalItem(val item: ProposalViewItem) : BaseItem() {

    override val layoutId = R.layout.proposal_list_item

    override val uniqueId = item.id

    override fun bind(holder: BaseViewHolder) {
        super.bind(holder)

        val countryFlag: RoundedImageView = holder.containerView.findViewById(R.id.proposal_item_country_flag)
        val countryName: TextView = holder.containerView.findViewById(R.id.proposal_item_country_name)
        val providerID: TextView = holder.containerView.findViewById(R.id.proposal_item_provider_id)
        val qualityLevel: ImageView = holder.containerView.findViewById(R.id.proposal_item_quality_level)

        countryFlag.setImageBitmap(item.countryFlagImage)
        countryName.text = item.countryName
        providerID.text = item.providerID
        qualityLevel.setImageResource(item.qualityResID)
    }
}

data class ProposalHeaderItem(val title: String) : BaseItem() {

    override val layoutId = R.layout.proposal_list_header_item

    override val uniqueId = title

    override fun bind(holder: BaseViewHolder) {
        super.bind(holder)
        val headerText: TextView = holder.containerView.findViewById(R.id.proposal_item_header_text)
        headerText.text = title
    }
}

