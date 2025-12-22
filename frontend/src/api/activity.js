const BASE_URL = 'http://localhost:8080/api/v1/activities';

/**
 * AI 추천 활동 목록 조회
 * @param {Object} req - ActivityRecommendReq 데이터
 * @returns {Promise<Object>} ActivityRecommendRes
 */
export async function getRecommendations(req) {
    try {
        const response = await fetch(`${BASE_URL}/recommend`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(req),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'AI 추천을 불러오는 중 오류가 발생했습니다.');
        }

        return await response.json();
    } catch (error) {
        console.error('API Error (getRecommendations):', error);
        throw error;
    }
}

/**
 * 사용자의 예상 충전 시간 조회
 * @param {number|string} userId - 사용자 ID
 * @returns {Promise<Object>} { estimatedTime: number }
 */
export async function getEstimatedTime(userId) {
    try {
        const response = await fetch(`${BASE_URL}/estimated-time/${userId}`);

        if (!response.ok) {
            throw new Error('예상 충전 시간을 불러오지 못했습니다.');
        }

        return await response.json();
    } catch (error) {
        console.error('API Error (getEstimatedTime):', error);
        throw error;
    }
}
