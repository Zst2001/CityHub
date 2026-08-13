import creativeMarket from '../assets/images/webp/creativemarket.webp'
import concert from '../assets/images/webp/concert.webp'
import artExhibition from '../assets/images/webp/artexhibition.webp'
import potteryClass from '../assets/images/webp/potteryclass.webp'
import culturalLecture from '../assets/images/webp/culturallecture.webp'
import nightRunning from '../assets/images/webp/nightrunning.webp'
import natureWorkshop from '../assets/images/webp/natureworkshop.webp'
import theatricalPerformance from '../assets/images/webp/theatricalperformance.webp'
import movieNight from '../assets/images/webp/movienight.webp'
import strollingThrough from '../assets/images/webp/strollingthrough.webp'
import coffeeMarket from '../assets/images/webp/coffeemarket.webp'
import floralWorkshop from '../assets/images/webp/floralworkshop.webp'
import heroImage from '../assets/images/webp/hero.webp'
import fallbackImage from '../assets/images/webp/fallback.webp'

const activityImages = {
  1: creativeMarket, 2: concert, 3: artExhibition, 4: potteryClass,
  5: culturalLecture, 6: nightRunning, 7: natureWorkshop, 8: theatricalPerformance,
  9: movieNight, 10: strollingThrough, 11: coffeeMarket, 12: floralWorkshop,
}

export const cityHubHeroImage = heroImage
export const activityFallbackImage = fallbackImage
export const getActivityImage = (activity) => activityImages[activity?.id] || fallbackImage
export const imageFallback = (event) => { event.target.src = fallbackImage }
