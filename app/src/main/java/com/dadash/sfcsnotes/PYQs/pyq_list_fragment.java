package com.dadash.sfcsnotes.PYQs;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dadash.sfcsnotes.Adapters.PYQ_List_Adapter;
import com.dadash.sfcsnotes.R;
import java.util.ArrayList;
import java.util.List;
public class pyq_list_fragment extends Fragment {
    private RecyclerView pyqRecyclerView;
    private PYQ_List_Adapter pyqAdapter;
    public pyq_list_fragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pyq_list, container, false);
        pyqRecyclerView = view.findViewById(R.id.pyqRecyclerView);
        pyqRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pyqAdapter = new PYQ_List_Adapter(getSampleData(), getFragmentManager(), requireContext());
        pyqRecyclerView.setAdapter(pyqAdapter);
        return view;
    }
    private List<String> getSampleData() {
        List<String> data = new ArrayList<>();
        data.add("Unit Test I");
        data.add("Half-Yearly");
        data.add("Unit Test II");
        data.add("Annual Exam");
        return data;
    }
}
