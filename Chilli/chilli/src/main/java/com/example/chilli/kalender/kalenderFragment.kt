package com.example.chilli.kalender

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.service.autofill.CustomDescription
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chilli.R
import com.example.chilli.broadcash.messageDetail.MessageDetailFragment
import com.example.chilli.database.AppDatabase
import com.example.chilli.database.Messages
import com.example.chilli.databinding.FragmentKalenderBinding
import com.example.chilli.viewModel.MessageViewModel
import com.example.chilli.viewModel.MessageViewModelFactory
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.text.SimpleDateFormat
import java.util.*


class kalenderFragment : Fragment(), eventKalenderAdapter.OnItemClickListener {
    private lateinit var binding: FragmentKalenderBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: eventKalenderAdapter
    private var data: MutableLiveData<List<Messages>> = MutableLiveData()
    private lateinit var today: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentKalenderBinding.inflate(inflater)
        today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val application = requireNotNull(this.activity).application
        val message = AppDatabase.getInstance(application).messagesDao
        val factory = MessageViewModelFactory(message, application)
        val viewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        adapter = eventKalenderAdapter(emptyList())
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)

        binding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.startCollectingData()

        viewModel.message.observe(viewLifecycleOwner) { data ->
            filterDataByDate(data, today)
        }

//        val datePin = data.value?.map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.pinTime).toString() }
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                GregorianCalendar(year, month, dayOfMonth).time
            )

            today = selectedDate
            filterDataByDate(viewModel.message.value ?: emptyList(), selectedDate)
        }

//        if (datePin != null) {
//            binding.calendarView.setColoredDates(datePin)
//        }

        return binding.root
    }


    private fun filterDataByDate(messages: List<Messages>, selectedDate: String) {
        val filteredData = messages.filter {
            if (it.pinTime != "") {
                val formattedPinTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.pinTime).toString()
                val selectedDateFormated = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate).toString()
                formattedPinTime == selectedDateFormated

            } else {
                false
            }
        }
        data.value = filteredData
        adapter.setData(filteredData)
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(item: Messages) {
        val bundle = Bundle().apply {
            putSerializable("idMessage", item)
        }
        val detailFragment = MessageDetailFragment()
        detailFragment.arguments = bundle
        view?.findNavController()?.navigate(R.id.action_kalenderFragment_to_messageDetailFragment, bundle)
    }


}