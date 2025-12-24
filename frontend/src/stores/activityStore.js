import { reactive, watch } from 'vue';

const STORAGE_KEY = 'wtd_activity_state';

const initialState = {
    isExpandedSearch: false,
    recommendations: [],
    hasSearched: false,
    filters: {
        chargeTime: 30,
        isEcoFriendly: false,
        travelTime: 5,
        selectedCategory: [],
        usePublicTransport: false,
        personnel: 1,
        selectedPurpose: [],
        selectedPreference: '',
    },
};

// Load initial state from sessionStorage if available
const savedState = sessionStorage.getItem(STORAGE_KEY);
const parsedState = savedState ? JSON.parse(savedState) : initialState;

export const activityStore = reactive(parsedState);

// Watch for changes and save to sessionStorage
watch(
    activityStore,
    (newState) => {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(newState));
    },
    { deep: true }
);

export const resetActivityStore = () => {
    Object.assign(activityStore, JSON.parse(JSON.stringify(initialState)));
    sessionStorage.removeItem(STORAGE_KEY);
};
