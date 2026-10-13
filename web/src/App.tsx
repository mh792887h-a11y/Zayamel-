import React, { useEffect, useState, useRef } from 'react';
import { supabase, signInWithGoogle, signOut } from './lib/supabase';
import { Category, Zamil, AUTHORIZED_OWNER_EMAIL } from './types';
import { 
  Play, Pause, Search, LogOut, RefreshCw, Music, 
  Heart, Share2, Sun, Moon, TrendingUp, Sparkles, 
  Volume2, FastForward, Rewind, SkipForward, SkipBack 
} from 'lucide-react';

export default function App() {
  const [session, setSession] = useState<any>(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [zawamil, setZawamil] = useState<Zamil[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentZamil, setCurrentZamil] = useState<Zamil | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTab, setCurrentTab] = useState<'home' | 'most_played' | 'favorites' | 'admin'>('home');
  const [favorites, setFavorites] = useState<Set<string>>(new Set());
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [audioQuality, setAudioQuality] = useState<'standard' | 'low' | 'high'>('standard');

  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
      const email = session?.user?.email?.toLowerCase();
      setIsAdmin(email === AUTHORIZED_OWNER_EMAIL.toLowerCase());
    });

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
      const email = session?.user?.email?.toLowerCase();
      setIsAdmin(email === AUTHORIZED_OWNER_EMAIL.toLowerCase());
    });

    fetchData();

    return () => subscription.unsubscribe();
  }, []);

  async function fetchData() {
    const { data: catData } = await supabase.from('categories').select('*').order('sort_order', { ascending: true });
    if (catData) setCategories(catData);

    const { data: zawData } = await supabase.from('zawamil').select('*').order('sort_order', { ascending: true });
    if (zawData) setZawamil(zawData);
  }

  function handlePlayZamil(zamil: Zamil) {
    setCurrentZamil(zamil);
    setIsPlaying(true);

    // تسجيل استماع فعلي بعد بدء التشغيل
    setTimeout(() => {
      supabase.rpc('record_zamil_play', { p_zamil_id: zamil.id });
    }, 5000);
  }

  function toggleFavorite(zamilId: string) {
    const newFavs = new Set(favorites);
    if (newFavs.has(zamilId)) {
      newFavs.delete(zamilId);
    } else {
      newFavs.add(zamilId);
    }
    setFavorites(newFavs);
  }

  function handleShare(zamil: Zamil) {
    if (navigator.share) {
      navigator.share({
        title: zamil.title,
        text: `استمع إلى زامل ${zamil.title} للمنشد ${zamil.performer}`,
        url: window.location.href
      }).catch(() => {});
    } else {
      navigator.clipboard.writeText(`${window.location.origin}/#zamil-${zamil.id}`);
      alert('تم نسخ رابط الزامل!');
    }
  }

  function handleNextTrack() {
    if (!currentZamil || zawamil.length === 0) return;
    const currentIndex = zawamil.findIndex(z => z.id === currentZamil.id);
    if (currentIndex !== -1 && currentIndex + 1 < zawamil.length) {
      handlePlayZamil(zawamil[currentIndex + 1]);
    }
  }

  function handlePrevTrack() {
    if (!currentZamil || zawamil.length === 0) return;
    const currentIndex = zawamil.findIndex(z => z.id === currentZamil.id);
    if (currentIndex > 0) {
      handlePlayZamil(zawamil[currentIndex - 1]);
    }
  }

  const filteredZawamil = zawamil.filter(z => {
    if (currentTab === 'favorites') {
      if (!favorites.has(z.id)) return false;
    }
    const matchCat = !selectedCategory || z.category_id === selectedCategory;
    const matchQuery = !searchQuery || 
      z.title.includes(searchQuery) || 
      z.performer.includes(searchQuery);
    return matchCat && matchQuery;
  }).sort((a, b) => {
    if (currentTab === 'most_played') {
      return (b.plays_count || 0) - (a.plays_count || 0);
    }
    return a.sort_order - b.sort_order;
  });

  return (
    <div dir="rtl" className={`min-h-screen ${isDarkMode ? 'bg-[#12100E] text-[#F5EFE6]' : 'bg-[#FBF8F3] text-[#26211C]'} font-sans pb-32 transition-colors`}>
      {/* Header */}
      <header className={`${isDarkMode ? 'bg-[#1C1916] border-[#292420]' : 'bg-white border-[#E0D7CC]'} border-b sticky top-0 z-40 px-4 py-3 shadow-md`}>
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#E5A93C]/20 text-[#E5A93C] flex items-center justify-center font-bold">
              <Music className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold">زوامل اليمن</h1>
              <p className="text-xs text-[#E5A93C]">
                {isAdmin ? 'لوحة التحكم' : 'منصة التراث والزوامل الأصيلة'}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsDarkMode(!isDarkMode)}
              className="p-2 rounded-lg border border-[#4A423B]/30 hover:bg-gray-500/10"
              title="تبديل المظهر"
            >
              {isDarkMode ? <Sun className="w-4 h-4 text-[#E5A93C]" /> : <Moon className="w-4 h-4 text-gray-700" />}
            </button>

            {isAdmin && (
              <button
                onClick={() => setCurrentTab(currentTab === 'admin' ? 'home' : 'admin')}
                className="px-3 py-1.5 rounded-lg text-xs font-bold bg-[#E5A93C] text-black hover:bg-[#C78C20] transition"
              >
                {currentTab === 'admin' ? 'الواجهة العامة' : 'لوحة التحكم'}
              </button>
            )}

            {!session ? (
              <button
                onClick={signInWithGoogle}
                className="flex items-center gap-2 px-3 py-1.5 bg-[#E5A93C] text-black rounded-lg text-xs font-bold"
              >
                <span>تسجيل الدخول بـ Google</span>
              </button>
            ) : (
              <button
                onClick={signOut}
                className="p-2 text-red-400 hover:bg-red-950/30 rounded-lg"
                title="تسجيل الخروج"
              >
                <LogOut className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-6xl mx-auto px-4 py-6">
        {currentTab === 'admin' && isAdmin ? (
          <div className="space-y-6">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-[#1C1916] p-4 rounded-xl border border-[#292420]">
                <p className="text-xs text-gray-400">إجمالي الزوامل</p>
                <p className="text-2xl font-bold text-[#E5A93C]">{zawamil.length}</p>
              </div>
              <div className="bg-[#1C1916] p-4 rounded-xl border border-[#292420]">
                <p className="text-xs text-gray-400">المنشورة</p>
                <p className="text-2xl font-bold text-green-400">{zawamil.filter(z => z.is_published).length}</p>
              </div>
              <div className="bg-[#1C1916] p-4 rounded-xl border border-[#292420]">
                <p className="text-xs text-gray-400">المسودات المخفية</p>
                <p className="text-2xl font-bold text-red-400">{zawamil.filter(z => !z.is_published).length}</p>
              </div>
              <div className="bg-[#1C1916] p-4 rounded-xl border border-[#292420]">
                <p className="text-xs text-gray-400">التصنيفات</p>
                <p className="text-2xl font-bold text-amber-300">{categories.length}</p>
              </div>
            </div>

            <div className="bg-[#1C1916] rounded-xl p-6 border border-[#292420]">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-bold">إدارة وترتيب الزوامل</h2>
                <button onClick={fetchData} className="flex items-center gap-1 text-xs text-[#E5A93C]">
                  <RefreshCw className="w-3.5 h-3.5" /> تحديث البيانات
                </button>
              </div>
              <div className="space-y-3">
                {zawamil.map(item => (
                  <div key={item.id} className="flex items-center justify-between p-3 bg-[#24201C] rounded-lg border border-[#3A332C]">
                    <div>
                      <p className="font-bold text-sm">{item.title}</p>
                      <p className="text-xs text-gray-400">{item.performer} • {item.plays_count || 0} استماع</p>
                    </div>
                    <span className={`text-[10px] px-2 py-0.5 rounded ${item.is_published ? 'bg-green-950 text-green-400' : 'bg-red-950 text-red-400'}`}>
                      {item.is_published ? 'منشور' : 'مخفي'}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Tabs */}
            <div className="flex items-center gap-2 overflow-x-auto pb-1">
              <button
                onClick={() => setCurrentTab('home')}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition ${currentTab === 'home' ? 'bg-[#E5A93C] text-black' : 'bg-gray-500/10'}`}
              >
                الرئيسية
              </button>
              <button
                onClick={() => setCurrentTab('most_played')}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1 ${currentTab === 'most_played' ? 'bg-[#E5A93C] text-black' : 'bg-gray-500/10'}`}
              >
                <TrendingUp className="w-3.5 h-3.5" /> الأكثر استماعاً
              </button>
              <button
                onClick={() => setCurrentTab('favorites')}
                className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-1 ${currentTab === 'favorites' ? 'bg-[#E5A93C] text-black' : 'bg-gray-500/10'}`}
              >
                <Heart className="w-3.5 h-3.5" /> المفضلة ({favorites.size})
              </button>
            </div>

            {/* Search */}
            <div className="relative">
              <input
                type="text"
                placeholder="ابحث عن اسم الزامل، المنشد، أو الوصف..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className={`w-full ${isDarkMode ? 'bg-[#1C1916] border-[#292420]' : 'bg-white border-[#E0D7CC]'} border rounded-xl px-10 py-3 text-sm focus:outline-none focus:border-[#E5A93C]`}
              />
              <Search className="w-4 h-4 text-gray-400 absolute right-3.5 top-3.5" />
            </div>

            {/* Categories */}
            <div className="flex items-center gap-2 overflow-x-auto pb-2">
              <button
                onClick={() => setSelectedCategory(null)}
                className={`px-4 py-1.5 rounded-full text-xs font-bold transition whitespace-nowrap ${!selectedCategory ? 'bg-[#E5A93C] text-black' : 'bg-gray-500/10'}`}
              >
                الكل
              </button>
              {categories.map(c => (
                <button
                  key={c.id}
                  onClick={() => setSelectedCategory(c.id)}
                  className={`px-4 py-1.5 rounded-full text-xs font-bold transition whitespace-nowrap ${selectedCategory === c.id ? 'bg-[#E5A93C] text-black' : 'bg-gray-500/10'}`}
                >
                  {c.name}
                </button>
              ))}
            </div>

            {/* Zawamil List */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {filteredZawamil.map(item => (
                <div
                  key={item.id}
                  className={`flex items-center justify-between p-4 ${isDarkMode ? 'bg-[#1C1916] hover:bg-[#25201C] border-[#292420]' : 'bg-white hover:bg-gray-50 border-[#E0D7CC]'} rounded-xl border transition`}
                >
                  <div className="flex items-center gap-3 cursor-pointer" onClick={() => handlePlayZamil(item)}>
                    <div className="w-12 h-12 rounded-lg bg-[#292420] overflow-hidden flex items-center justify-center">
                      {item.cover_path ? (
                        <img src={item.cover_path} alt={item.title} className="w-full h-full object-cover" />
                      ) : (
                        <Music className="w-6 h-6 text-[#E5A93C]" />
                      )}
                    </div>
                    <div>
                      <p className="font-bold text-sm">{item.title}</p>
                      <p className="text-xs text-[#E5A93C]">{item.performer} • {item.plays_count || 0} استماع</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => toggleFavorite(item.id)}
                      className="p-2 text-gray-400 hover:text-red-500 transition"
                    >
                      <Heart className={`w-4 h-4 ${favorites.has(item.id) ? 'fill-red-500 text-red-500' : ''}`} />
                    </button>
                    <button
                      onClick={() => handleShare(item)}
                      className="p-2 text-gray-400 hover:text-[#E5A93C] transition"
                    >
                      <Share2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handlePlayZamil(item)}
                      className="w-9 h-9 rounded-full bg-[#E5A93C] text-black flex items-center justify-center hover:scale-105 transition"
                    >
                      <Play className="w-4 h-4 fill-current ml-0.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Continuous Audio Player Bar */}
      {currentZamil && (
        <div className={`fixed bottom-0 left-0 right-0 ${isDarkMode ? 'bg-[#1C1916] border-[#292420]' : 'bg-white border-[#E0D7CC]'} border-t p-3 shadow-2xl z-50`}>
          <div className="max-w-6xl mx-auto flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-[#292420] overflow-hidden">
                {currentZamil.cover_path && <img src={currentZamil.cover_path} alt="" className="w-full h-full object-cover" />}
              </div>
              <div>
                <p className="font-bold text-xs">{currentZamil.title}</p>
                <p className="text-[11px] text-[#E5A93C]">{currentZamil.performer}</p>
              </div>
            </div>

            <div className="flex items-center gap-3">
              <button onClick={handlePrevTrack} className="p-2 text-gray-400 hover:text-white">
                <SkipBack className="w-4 h-4" />
              </button>

              <audio
                ref={audioRef}
                controls
                autoPlay
                onEnded={handleNextTrack}
                src={
                  audioQuality === 'low' && currentZamil.audio_path_low 
                    ? currentZamil.audio_path_low 
                    : audioQuality === 'high' && currentZamil.audio_path_high 
                      ? currentZamil.audio_path_high 
                      : currentZamil.audio_path
                }
                className="h-9 w-64 max-w-full"
              />

              <button onClick={handleNextTrack} className="p-2 text-gray-400 hover:text-white">
                <SkipForward className="w-4 h-4" />
              </button>

              {/* Quality Switcher */}
              <select
                value={audioQuality}
                onChange={e => setAudioQuality(e.target.value as any)}
                className="bg-transparent border border-[#4A423B] rounded text-xs p-1"
              >
                <option value="standard">128k</option>
                <option value="low">64k</option>
                <option value="high">320k</option>
              </select>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
