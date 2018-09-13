package welvoipclient.com.welvoipclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Krystiano on 2016-11-28.
 */

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsVH> {
    Context context;
    List<Contacts> dataArray;

    OnItemClickListener clickListener;

    public ContactsAdapter(Context context, List<Contacts> dataArray) {
        this.context = context;
        this.dataArray = dataArray;

    }


    @Override
    public ContactsVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.model, parent, false);
        ContactsVH viewHolder = new ContactsVH(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContactsVH holder, int position) {

        holder.firstNameTextView.setText(dataArray.get(position).first_name);
        holder.lastNameTextView.setText(dataArray.get(position).last_name);
        holder.ipAddressTextView.setText(dataArray.get(position).ip_address);

    }

    @Override
    public int getItemCount() {
        return dataArray.size();
    }

    class ContactsVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView firstNameTextView,lastNameTextView,ipAddressTextView;

        public ContactsVH(View itemView) {
            super(itemView);

            firstNameTextView = (TextView) itemView.findViewById(R.id.first_name);
            lastNameTextView = (TextView) itemView.findViewById(R.id.last_name);
            ipAddressTextView = (TextView) itemView.findViewById(R.id.ip_address);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}
