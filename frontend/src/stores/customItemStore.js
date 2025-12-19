import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from '@/api/axios' // Correct path to global axios instance
import { AVATAR_DECORATIONS } from '@/constants/avatarConfig'
import { POPOVER_DECORATIONS } from '@/constants/popoverConfig'
import { PROFILE_THEMES } from '@/constants/themeConfig'

export const useCustomItemStore = defineStore('customItem', () => {
    // State
    const avatarItems = ref([])
    const popoverItems = ref([])
    const themeItems = ref([])
    const loading = ref(false)

    const itemDefinitions = ref({}) // Dictionary of all items by ID ~ { id: config } to merge into static constants

    // Actions
    const fetchItemDefinitions = async () => {
        try {
            const res = await axios.get('/items/definitions');
            // Convert list to map
            const defs = {};
            res.data.forEach(item => {
                // item.config is Object or String?
                // Backend returns list of objects.
                // Assuming config is properly serialized/deserialized by Jackson if we used Object in DTO.
                // Wait, Controller returns Domain Entity `CustomItem`. 
                // Domain Entity `config` is String (JSON).
                // So we need to parse it if it is a string.
                let config = item.config;
                if (typeof config === 'string') {
                    try {
                        config = JSON.parse(config);
                    } catch (e) {
                         console.error('Failed to parse item config', item);
                    }
                }
                
                // Overwrite 'Cybercity' config with local constant to ensure animation updates apply immediately
                // regardless of stale DB data.
                if (item.name === 'Cybercity') {
                   if (item.itemType === 'POPOVER' && POPOVER_DECORATIONS.cybercity) {
                       config = { ...config, ...POPOVER_DECORATIONS.cybercity };
                   } else if (item.itemType === 'THEME' && PROFILE_THEMES.cybercity) {
                       config = { ...config, ...PROFILE_THEMES.cybercity };
                   }
                }
                
                defs[item.id] = {
                    id: item.id,
                    name: item.name,
                    itemType: item.itemType,
                    ...config
                };
            });
            itemDefinitions.value = defs;
        } catch (e) {
            console.error('Failed to fetch item definitions', e);
        }
    }

    const fetchUserItems = async () => {
        loading.value = true
        // Ensure definitions are loaded first or in parallel, but we need them for rendering "others" anyway.
        // Actually, let's load definitions if empty.
        if (Object.keys(itemDefinitions.value).length === 0) {
            await fetchItemDefinitions();
        }

        try {
            // Fetch dynamic items from backend (My Owned Items)
            const [avatars, popovers, themes] = await Promise.all([
                axios.get('/items/my', { params: { type: 'AVATAR' } }),
                axios.get('/items/my', { params: { type: 'POPOVER' } }),
                axios.get('/items/my', { params: { type: 'THEME' } })
            ]);

            // Merge owned items with definitions or static defaults
            // Actually, `getMyItems` returns CustomItemResponse which HAS config.
            // But we might want to unify the source of truth for "what exists" vs "what I have".
            // The existing `avatarConfig` logic expects an Object of "Available Items".
            
            // For now, let's just make the "Available Items" be the defaults + what I own.
            // Wait, the requirement is "Everyone can see others' themes". 
            // So if I view someone else's profile, I need to know what "Theme ID 5" is.
            // That comes from `fetchItemDefinitions`.
            
            // So `avatarItems` state should probably be `All Definitions` (for rendering) 
            // OR `My Available Choices` (for editing).
            
            // `UserAvatar` component uses `AVATAR_DECORATIONS`. 
            // We should replace that usage with `store.getAvatarConfig(id)`.
            
            // For editing profile, we need "My Owned Items".
            
            // Let's store "My Items" separately.
            
            avatarItems.value = formatMyItems(avatars.data, AVATAR_DECORATIONS)
            popoverItems.value = formatMyItems(popovers.data, POPOVER_DECORATIONS)
            themeItems.value = formatMyItems(themes.data, PROFILE_THEMES)

        } catch (error) {
            console.error('Failed to fetch user items', error)
            // Even in error case, we must inject keys into static items
            avatarItems.value = formatMyItems([], AVATAR_DECORATIONS)
            popoverItems.value = formatMyItems([], POPOVER_DECORATIONS)
            themeItems.value = formatMyItems([], PROFILE_THEMES)
        } finally {
            loading.value = false
        }
    }

    const formatMyItems = (backendItems, staticDefaults) => {
        const result = {};

        // 1. UI 루프를 위해 정적 기본값에 키 주입
        if (staticDefaults) {
            Object.keys(staticDefaults).forEach(key => {
                const item = staticDefaults[key];
                result[key] = {
                    key: key,      // 중요: UI 루프를 위한 키 주입
                    id: key,       // UI 통일성을 위해 키를 ID로 사용
                    ...item
                };
            });
        }

        // 2. 백엔드 아이템 병합 (이미 'id'를 가지고 있음)
        if (backendItems) {
            backendItems.forEach(item => {
                 let config = item.config;
                 if (typeof config === 'string') {
                     try { config = JSON.parse(config); } catch(e){}
                 }
                 result[item.id] = {
                    id: item.id,
                    name: item.name,
                    description: item.description,
                    isDefault: item.isDefault,
                    isOwned: item.isOwned,
                    ...config
                 }
            });
        }
        return result;
    }

    // 키로 설정 가져오기 헬퍼 (정적 키와 숫자 ID 모두 작동)
    const getItemConfig = (type, key) => {
        // 정의(동적)를 먼저 확인하고, 정적 기본값을 확인
        // 하지만 definition은 ID로 평탄화되어 있음.
        // 정적 기본값은 'cybercity' 같은 키를 사용.
        
        // 키가 정의에 있다면 (숫자 또는 DB ID와 일치하는 문자열)
        if (itemDefinitions.value[key]) return itemDefinitions.value[key];
        
        // 정적 파일로 폴백 (상단에서 가져옴)
        if (type === 'AVATAR') return AVATAR_DECORATIONS[key];
        if (type === 'POPOVER') return POPOVER_DECORATIONS[key];
        if (type === 'THEME') return PROFILE_THEMES[key];
        
        return null;
    }

    return {
        avatarItems,
        popoverItems,
        themeItems,
        fetchUserItems,
        fetchItemDefinitions,
        getItemConfig,
        loading
    }
})


