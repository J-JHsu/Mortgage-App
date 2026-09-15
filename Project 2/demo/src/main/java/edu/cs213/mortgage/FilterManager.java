package edu.cs213.mortgage;

import java.util.ArrayList;
import java.util.List;
import static java.util.Comparator.comparing;


public class FilterManager {
    private final List<Filter> filters;

    public FilterManager() {
        this.filters = new ArrayList<>();
    }

    public FilterManager(List<Filter> filters) {
        this.filters = new ArrayList<>(filters);
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }

    /**
     * Builds the SQL WHERE clause from active filters.
     * Includes default action_taken and purchaser_type conditions.
     * @return A complete WHERE clause for the SQL query.
     */
    public String buildWhereClause() {
        if (filters.isEmpty()) {
            return "WHERE a.action_taken = 1 AND a.purchaser_type IN (0, 1, 2, 3, 4, 8)";
        }

        StringBuilder whereClause = new StringBuilder(
                "WHERE a.action_taken = 1 AND a.purchaser_type IN (0, 1, 2, 3, 4, 8)"
        );

        filters.sort(comparing(Filter::getChoice));

        for (int i = 0; i < filters.size(); i++) 
        {
            if (i == 0)
            {
                if (i + 1 < filters.size())
                {
                    if (filters.get(i+1).getChoice() == filters.get(i).getChoice())
                    {
                        whereClause.append(" AND (").append(filters.get(i).getSqlCondition());
                    }
                    else
                    {
                        whereClause.append(" AND ").append(filters.get(i).getSqlCondition());
                    }
                }
                else
                {
                    whereClause.append(" AND ").append(filters.get(i).getSqlCondition());
                }
            }
            else
            {
                if (filters.get(i).getChoice() == filters.get(i-1).getChoice())
                {
                    if (i + 1 < filters.size())
                    {
                        if (filters.get(i+1).getChoice() == filters.get(i).getChoice())
                        {
                            whereClause.append(" OR ").append(filters.get(i).getSqlCondition());
                        }
                        else
                        {
                            whereClause.append(" OR ").append(filters.get(i).getSqlCondition()).append(")");
                        }
                    }
                    else
                    {
                        whereClause.append(" OR ").append(filters.get(i).getSqlCondition()).append(")");
                    }
                }
                else
                {
                    if (i + 1 < filters.size())
                    {
                        if (filters.get(i+1).getChoice() == filters.get(i).getChoice())
                        {
                            whereClause.append(" AND (").append(filters.get(i).getSqlCondition());
                        }
                        else
                        {
                            whereClause.append(" AND ").append(filters.get(i).getSqlCondition());
                        }
                    }
                    else
                    {
                        whereClause.append(" AND ").append(filters.get(i).getSqlCondition());
                    }
                }
            }
        }
        return whereClause.toString();
    }   
}
