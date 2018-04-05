package br.com.gsw.githubsearch.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.gsw.githubsearch.R;
import timber.log.Timber;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.ViewHolder> {

    private Repository[] mRepositories;
    private final int cardLayout;
    private final Context mContext;

    public RepositoryAdapter(Repository[] list, Context context) {

        this.mRepositories = list;
        this.cardLayout = R.layout.repository_list_content;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(cardLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Repository currentRepo = mRepositories[position];

        if (currentRepo != null) {

            if (currentRepo.getName() != null &&
                    !currentRepo.getName().isEmpty()) {
                holder.name.setText(currentRepo.getName());
            }

            if (currentRepo.getDescription() != null &&
                    !currentRepo.getDescription().isEmpty()) {
                holder.description.setText(currentRepo.getDescription());
            }

            if (currentRepo.getLink() != null &&
                    !currentRepo.getLink().isEmpty()) {
                holder.link.setText(currentRepo.getLink());
            }

            if (currentRepo.geAvatarUrl() != null &&
                    !currentRepo.geAvatarUrl().isEmpty()) {
                Picasso.with(mContext)
                        .load(currentRepo.geAvatarUrl())
                        .placeholder(R.drawable.ic_sync)
                        .error(R.drawable.ic_sync_error)
                        .fit()
                        .centerCrop()
                        .into(holder.image);
            }

            holder.itemView.setTag(currentRepo);
            holder.itemView.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mRepositories == null ? 0 : mRepositories.length;
    }

    @Override
    public long getItemId(int item) {
        return item;
    }

    public void clearData() {
        if (mRepositories != null)
            mRepositories = new Repository[0];

        notifyDataSetChanged();
    }

    //Since the source compatibility is set to Java 8, I'm using a lambda statement for code readability
    private final View.OnClickListener mOnClickListener = view -> {
        Repository repo = (Repository) view.getTag();
        String repoLink = repo.getLink();
        Context context = view.getContext();

        if(repoLink != null && !repoLink.isEmpty()){
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(repo.getLink()));
            context.startActivity(webIntent);
        }
        else{
            Snackbar snackbar = Snackbar
                    .make(view, context.getString(R.string.error_opening_repository),
                            Snackbar.LENGTH_LONG);

            snackbar.show();

            Timber.e(context.getString(R.string.error_opening_repository));
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView description;
        final TextView link;
        final ImageView image;

        ViewHolder(View itemView) {

            super(itemView);

            name = itemView.findViewById(R.id.repo_name);
            description = itemView.findViewById(R.id.repo_description);
            link = itemView.findViewById(R.id.repo_link);
            image = itemView.findViewById(R.id.repo_image);
        }
    }
}
