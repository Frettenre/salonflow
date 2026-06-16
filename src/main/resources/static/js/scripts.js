// Generates and slide-animates CSS alert banners
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');

    let bgClass = "bg-white border-slate-200 text-slate-800";
    let icon = "info";
    let iconColor = "text-blue-500";

    if (type === 'success') {
        bgClass = "bg-emerald-50 border-emerald-100 text-emerald-950";
        icon = "check-circle-2";
        iconColor = "text-emerald-600";
    } else if (type === 'error') {
        bgClass = "bg-rose-50 border-rose-100 text-rose-950";
        icon = "alert-circle";
        iconColor = "text-rose-600";
    }

    toast.className = `p-4 rounded-2xl border shadow-lg flex items-center gap-3 pointer-events-auto transition-all duration-300 transform translate-y-12 opacity-0 ${bgClass}`;
    toast.innerHTML = `
        <i data-lucide="${icon}" class="w-5 h-5 shrink-0 ${iconColor}"></i>
        <span class="text-xs font-extrabold leading-normal">${message}</span>
    `;

    container.appendChild(toast);
    lucide.createIcons();

    setTimeout(() => {
        toast.classList.remove('translate-y-12', 'opacity-0');
    }, 10);

    setTimeout(() => {
        toast.classList.add('opacity-0', 'scale-95');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 4000);
}

// State variables representing reactive state
const state = {
    currentView: 'home',
    loginMode: 'customer',
    user: null,
    categories: [],
    recommended: [],
    bookings: [],
    selectedSalon: null,
    selectedSalonServices: [],
    selectedSalonBookings: [],
    selectedSalonBlockedSlots: [],
    selectedService: null,
    selectedDate: '',
    selectedTime: null,
    partnerBlockedSlots: [],
    partnerBookings: [],
    partnerServices: [],
    partnerCustomers: [],
    selectedContact: '',
    partnerSalonsList: [],
    partnerSalon: null,
    selectedPartnerClientContact: '',
    selectedPartnerService: null,
    selectedPartnerDate: '',
    selectedPartnerTime: null
};

const translations = {
    en: {
        "hero.title": "Discover specialists matching your style",
        "hero.subtitle": "Effortlessly book premium styling, wellness, and care. Hand-vetted salons, tailored for you.",
        "recommended.title": "Recommended Salons",
        "categories.title": "Browse Categories",
        "lang.toggle": "RO",
        "login.title": "Welcome to SalonFlow",
        "login.subtitle": "Bilingual salon appointments styled to perfection",
        "login.email.label": "Email Address",
        "login.btn.send": "Get Verification Code",
        "login.otp.label": "Enter 6-Digit Code",
        "login.btn.verify": "Verify & Log In",
        "reg.title": "Complete Registration Profile",
        "reg.subtitle": "Please fill in the required fields below to register your new account.",
        "reg.firstname": "First Name *",
        "reg.lastname": "Surname *",
        "reg.phone": "Phone Number *",
        "reg.birthday": "Birthday (optional)",
        "reg.avatar": "Select Profile Avatar",
        "reg.btn.submit": "Complete Registration"
    },
    ro: {
        "hero.title": "Găsește specialiști dedicați stilului tău",
        "hero.subtitle": "Rezervă servicii de excepție rapid în platforma noastră elegantă. Saloane verificate, adaptate pentru tine.",
        "recommended.title": "Saloane Recomandate",
        "categories.title": "Categorii de servicii",
        "lang.toggle": "EN",
        "login.title": "Bun venit pe SalonFlow",
        "login.subtitle": "Programări rapide la saloane prestigioase",
        "login.email.label": "Adresă de E-mail",
        "login.btn.send": "Trimite Codul",
        "login.otp.label": "Introdu Codul din 6 Cifre",
        "login.btn.verify": "Verifică și Conectează-te",
        "reg.title": "Completează Profilul",
        "reg.subtitle": "Te rugăm să completezi câmpurile obligatorii de mai jos pentru a-ți crea contul.",
        "reg.firstname": "Prenume *",
        "reg.lastname": "Nume de Familie *",
        "reg.phone": "Număr de Telefon *",
        "reg.birthday": "Data Nașterii (opțional)",
        "reg.avatar": "Selectează Fotografia de Profil",
        "reg.btn.submit": "Finalizează Înregistrarea"
    }
};

state.currentLang = 'en';

function translateUI() {
    const lang = state.currentLang;
    document.querySelectorAll('[data-translate]').forEach(element => {
        const key = element.getAttribute('data-translate');
        if (translations[lang] && translations[lang][key]) {
            element.innerText = translations[lang][key];
        }
    });

    const langBtn = document.getElementById('lang-toggle-btn');
    if (langBtn) {
        langBtn.innerText = translations[lang]["lang.toggle"];
    }
}

function toggleLanguage() {
    state.currentLang = state.currentLang === 'en' ? 'ro' : 'en';
    translateUI();
    renderHomeFeed();
}

const presetAvatars = [
    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=150&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=150&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=150&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=150&auto=format&fit=crop"
];
let selectedRegAvatar = presetAvatars[0];

const workingHours = ["09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"];

window.addEventListener('load', () => {
    const savedContact = localStorage.getItem('user_contact');
    if (savedContact) {
        fetchUserProfile(savedContact);
    } else {
        navigateTo('login');
    }
    lucide.createIcons();

    const todayStr = new Date().toISOString().split('T')[0];
    document.getElementById('booking-date').value = todayStr;
    document.getElementById('block-slot-date').value = todayStr;
    document.getElementById('partner-booking-date').value = todayStr;

    const birthdayInput = document.getElementById('reg-birthday-input');
    if (birthdayInput) {
        birthdayInput.min = "1900-01-01";
        birthdayInput.max = todayStr;
    }

    state.selectedDate = todayStr;
    state.selectedPartnerDate = todayStr;

    document.getElementById('booking-date').addEventListener('change', (e) => {
        state.selectedDate = e.target.value;
        state.selectedTime = null;
        renderBookingHours();
    });
    document.getElementById('block-slot-date').addEventListener('change', () => {
        renderBlockSlotHours();
    });
    document.getElementById('partner-booking-date').addEventListener('change', (e) => {
        state.selectedPartnerDate = e.target.value;
        state.selectedPartnerTime = null;
        renderPartnerBookingHours();
    });
});

function setLoginMode(mode) {
    const customerTab = document.getElementById('login-tab-customer');
    const partnerTab = document.getElementById('login-tab-partner');
    const loginCard = document.querySelector('#view-login > div');
    const loginLogoBadge = document.getElementById('login-logo-badge');
    const emailLabel = document.querySelector('[data-translate="login.email.label"]');

    if (mode === 'customer') {
        customerTab.className = "py-2.5 rounded-xl text-xs font-black transition-all bg-white text-slate-800 shadow-xs cursor-pointer";
        partnerTab.className = "py-2.5 rounded-xl text-xs font-black transition-all text-slate-500 hover:text-slate-800 cursor-pointer";
        loginCard.className = "bg-white rounded-3xl border border-slate-200 shadow-sm p-8 space-y-6 transition-all duration-350";
        loginLogoBadge.className = "inline-flex p-4 rounded-2xl bg-blue-50 text-blue-600 border border-blue-100";
        if (emailLabel) emailLabel.className = "text-[10px] font-black text-slate-400 uppercase tracking-widest block";
    } else {
        customerTab.className = "py-2.5 rounded-xl text-xs font-black transition-all text-slate-400 hover:text-slate-200 cursor-pointer";
        partnerTab.className = "py-2.5 rounded-xl text-xs font-black transition-all bg-slate-800 text-white shadow-xs cursor-pointer";
        loginCard.className = "bg-slate-900 border border-slate-800 text-white rounded-3xl shadow-xl p-8 space-y-6 transition-all duration-350";
        loginLogoBadge.className = "inline-flex p-4 rounded-2xl bg-amber-500/10 text-amber-500 border border-amber-550/10";
        if (emailLabel) emailLabel.className = "text-[10px] font-black text-slate-500 uppercase tracking-widest block";
    }
    document.getElementById('login-error').classList.add('hidden');
}

function navigateTo(viewName) {
    state.currentView = viewName;

    document.getElementById('view-login').classList.add('hidden');
    document.getElementById('view-home').classList.add('hidden');
    document.getElementById('view-salon').classList.add('hidden');
    document.getElementById('view-partner').classList.add('hidden');
    document.getElementById('view-category').classList.add('hidden');
    document.getElementById('view-profile').classList.add('hidden');

    const reviewsView = document.getElementById('view-reviews');
    if (reviewsView) {
        reviewsView.classList.add('hidden');
    }

    if (viewName === 'login') {
        document.getElementById('view-login').classList.remove('hidden');
    } else if (viewName === 'home') {
        document.getElementById('view-home').classList.remove('hidden');
        fetchHomeData();
    } else if (viewName === 'salon') {
        document.getElementById('view-salon').classList.remove('hidden');
    } else if (viewName === 'partner') {
        document.getElementById('view-partner').classList.remove('hidden');
        fetchPartnerWorkspace();
    } else if (viewName === 'category') {
        document.getElementById('view-category').classList.remove('hidden');
    } else if (viewName === 'profile') {
        document.getElementById('view-profile').classList.remove('hidden');
        openProfileWorkspace();
    } else if (viewName === 'reviews') {
        if (reviewsView) {
            reviewsView.classList.remove('hidden');
        }
    }
    renderNavActions();
    translateUI();
    lucide.createIcons();
}

async function fetchUserProfile(contact) {
    try {
        const res = await fetch(`/api/users/profile?contact=${encodeURIComponent(contact)}`);
        if (res.ok) {
            const data = await res.json();
            state.user = data.user;
            navigateTo('home');
        } else {
            localStorage.removeItem('user_contact');
            navigateTo('login');
        }
    } catch (err) {
        console.error(err);
        navigateTo('login');
    }
}

async function fetchHomeData() {
    const contact = localStorage.getItem('user_contact') || "";
    try {
        const res = await fetch('/api/home', {
            headers: { 'X-User-Contact': contact }
        });
        const data = await res.json();
        state.categories = data.categories;
        state.recommended = data.recommended;
        state.bookings = data.bookings;

        renderHomeFeed();
    } catch (err) {
        console.error(err);
    }
}

function renderHomeFeed() {
    const bookingsCard = document.getElementById('active-bookings-card');
    const bookingsList = document.getElementById('active-bookings-list');
    if (state.bookings && state.bookings.length > 0) {
        bookingsCard.classList.remove('hidden');
        bookingsList.innerHTML = state.bookings.map(b => {
            const srvName = state.currentLang === 'ro' ? (b.service?.nameRo || b.service?.nameEn) : b.service?.nameEn;
            return `
                    <div class="p-4 rounded-2xl bg-slate-50 border border-slate-150/40 flex items-center justify-between group">
                        <div>
                            <h6 class="font-extrabold text-slate-800 text-xs">${b.salon?.name || 'Salon'}</h6>
                            <p class="text-[11px] text-slate-500 mt-0.5">${srvName || 'Service'}</p>
                            <p class="mt-2 text-[9px] font-black text-slate-600 bg-white border border-slate-200 inline-flex items-center gap-1 px-2 py-0.5 rounded-lg shadow-2xs">
                                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                                <span>${formatFriendlyDate(b.bookingDateTime)}</span>
                            </p>
                        </div>
                        <button onclick="cancelBooking(${b.id})" class="p-2 text-slate-400 hover:text-red-500 rounded-xl hover:bg-red-50 cursor-pointer transition-colors">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                        </button>
                    </div>
                `;
        }).join('');
    } else {
        bookingsCard.classList.add('hidden');
    }

    const salonsGrid = document.getElementById('recommended-salons-grid');
    // Slice state.recommended to display only the top 5 recommended salons
    const top5Salons = (state.recommended || []).slice(0, 5);
    salonsGrid.innerHTML = top5Salons.map(s => {
        const description = (state.currentLang === 'ro' ? s.descriptionRo : s.descriptionEn) || s.description || '';
        const ratingValue = s.rating ? s.rating.toFixed(1) : "5.0";
        return `
                <div onclick="openSalonDetail(${s.id})" class="bg-white rounded-3xl overflow-hidden shadow-2xs border border-slate-200 transition-all hover:-translate-y-1.5 duration-350 h-full flex flex-col cursor-pointer group animate-fade-in">
                    <div class="h-44 bg-slate-100 overflow-hidden">
                        <img src="${s.imageUrl}" referrerpolicy="no-referrer" alt="${s.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
                    </div>
                    <div class="p-5 flex flex-col flex-1 justify-between space-y-4">
                        <div>
                            <div class="flex items-start justify-between gap-2">
                                <h6 class="font-extrabold text-slate-800 text-sm group-hover:text-blue-600 transition-colors">${s.name}</h6>
                                <div class="flex items-center gap-1 bg-yellow-50 text-yellow-800 border border-yellow-100 px-2.5 py-1 rounded-xl text-[10px] font-black shrink-0">
                                    <i data-lucide="star" class="w-3 h-3 fill-yellow-500 text-yellow-500"></i>
                                    <span>${ratingValue}</span>
                                </div>
                            </div>
                            <p class="text-[11px] text-slate-500 line-clamp-2 leading-relaxed mt-1.5">${description}</p>
                        </div>
                        <button class="w-full py-2.5 rounded-xl border border-slate-200 hover:bg-blue-600 hover:text-white hover:border-blue-600 text-slate-700 text-xs font-black transition-all">Book Now</button>
                    </div>
                </div>
            `;
    }).join('');

    const categoriesGrid = document.getElementById('categories-grid');
    categoriesGrid.innerHTML = state.categories.map(c => {
        const catName = state.currentLang === 'ro' ? c.nameRo : c.nameEn;
        return `
                <div onclick="openCategoryDetail(${c.id})" class="bg-white rounded-2xl p-5 border border-slate-200 text-center hover:border-blue-300 cursor-pointer transition-all hover:-translate-y-1 duration-300">
                    <div class="w-12 h-12 mx-auto bg-slate-50 text-slate-700 rounded-xl flex items-center justify-center mb-3">
                        <i data-lucide="layout-template" class="w-6 h-6"></i>
                    </div>
                    <h6 class="text-xs font-black text-slate-800">${catName}</h6>
                </div>
            `;
    }).join('');

    lucide.createIcons();
}

async function openCategoryDetail(categoryId) {
    try {
        const res = await fetch(`/api/category/${categoryId}`);
        if (res.ok) {
            const data = await res.json();
            const catName = state.currentLang === 'ro' ? data.category.nameRo : data.category.nameEn;
            document.getElementById('category-title').innerText = catName;

            const salonsGrid = document.getElementById('category-salons-grid');
            if (data.salons && data.salons.length > 0) {
                salonsGrid.innerHTML = data.salons.map(s => {
                    const description = (state.currentLang === 'ro' ? s.descriptionRo : s.descriptionEn) || s.description || '';
                    const ratingValue = s.rating ? s.rating.toFixed(1) : "5.0";
                    return `
                            <div onclick="openSalonDetail(${s.id})" class="bg-white rounded-3xl overflow-hidden shadow-2xs border border-slate-200 transition-all hover:-translate-y-1.5 duration-350 h-full flex flex-col cursor-pointer group animate-fade-in">
                                <div class="h-44 bg-slate-100 overflow-hidden">
                                    <img src="${s.imageUrl}" referrerpolicy="no-referrer" alt="${s.name}" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
                                </div>
                                <div class="p-5 flex flex-col flex-1 justify-between space-y-4">
                                    <div>
                                        <div class="flex items-start justify-between gap-2">
                                            <h6 class="font-extrabold text-slate-800 text-sm group-hover:text-blue-600 transition-colors">${s.name}</h6>
                                            <div class="flex items-center gap-1 bg-yellow-50 text-yellow-800 border border-yellow-100 px-2.5 py-1 rounded-xl text-[10px] font-black shrink-0">
                                                <i data-lucide="star" class="w-3 h-3 fill-yellow-500 text-yellow-500"></i>
                                                <span>${ratingValue}</span>
                                            </div>
                                        </div>
                                        <p class="text-[11px] text-slate-500 line-clamp-2 leading-relaxed mt-1.5">${description}</p>
                                    </div>
                                    <button class="w-full py-2.5 rounded-xl border border-slate-200 hover:bg-blue-600 hover:text-white hover:border-blue-600 text-slate-700 text-xs font-black transition-all">Book Now</button>
                                </div>
                            </div>
                        `;
                }).join('');
            } else {
                salonsGrid.innerHTML = `
                            <div class="col-span-full py-12 text-center text-slate-400 text-xs font-semibold bg-white border border-slate-200 rounded-3xl">
                                No salons currently registered in this category.
                            </div>`;
            }
            navigateTo('category');
        }
    } catch (err) {
        console.error(err);
    }
}

function renderNavActions() {
    const navActions = document.getElementById('nav-actions');
    if (state.currentView === 'login') {
        navActions.innerHTML = '';
        return;
    }

    let langButton = `<button id="lang-toggle-btn" onclick="toggleLanguage()" class="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 cursor-pointer transition-colors">${state.currentLang === 'en' ? 'RO' : 'EN'}</button>`;

    if (state.user) {
        let partnerPortalBtn = '';
        if (state.user.partnerApproved) {
            if (state.currentView === 'partner') {
                partnerPortalBtn = `<button onclick="navigateTo('home')" class="px-3.5 py-1.5 rounded-xl border border-blue-200 text-xs font-extrabold text-blue-600 bg-blue-50/50 hover:bg-blue-50 transition-colors">Customer Mode</button>`;
            } else {
                partnerPortalBtn = `<button onclick="navigateTo('partner')" class="px-3.5 py-1.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-extrabold shadow-sm transition-colors">Partner Portal</button>`;
            }
        }
        navActions.innerHTML = `
          ${langButton}
          ${partnerPortalBtn}
          
          <div onclick="navigateTo('profile')" class="flex items-center gap-3 pl-3 border-l border-slate-200 cursor-pointer group hover:opacity-80 transition-opacity">
              <img src="${state.user.photoUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150&auto=format&fit=crop'}" referrerpolicy="no-referrer" alt="Profile" class="w-8 h-8 rounded-full object-cover border-2 border-blue-500">
              <div class="hidden sm:block text-left">
                  <p class="text-xs font-black text-slate-800 leading-none group-hover:text-blue-600 transition-colors">${state.user.firstName} ${state.user.lastName}</p>
                  <p class="text-[9px] text-slate-400 font-semibold">${state.user.contact}</p>
              </div>
          </div>
          
          <button onclick="logout()" class="p-2 text-slate-400 hover:text-red-500 transition-colors bg-slate-50 hover:bg-red-50 rounded-xl cursor-pointer">
              <i data-lucide="log-out" class="w-4 h-4"></i>
          </button>
      `;
    } else {
        navActions.innerHTML = `${langButton} <button onclick="navigateTo('login')" class="px-4 py-2 bg-blue-600 text-white font-bold text-xs rounded-xl shadow-md">Sign In</button>`;
    }
    lucide.createIcons();
}

async function sendOtp() {
    const inputVal = document.getElementById('login-contact-input').value.trim();
    if (!inputVal) {
        showToast("Please enter your email address.", "error");
        return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(inputVal)) {
        showToast("Te rugăm să introduci o adresă de e-mail validă! / Please enter a valid email address.", "error");
        return;
    }

    document.getElementById('login-error').classList.add('hidden');
    try {
        const res = await fetch('/api/auth/send-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contact: inputVal })
        });
        const data = await res.json();
        if (res.ok) {
            state.selectedContact = inputVal.toLowerCase();
            const simulatedOtpDisplay = document.getElementById('simulated-otp-display');
            if (data.otp) {
                simulatedOtpDisplay.innerText = data.otp;
            } else {
                simulatedOtpDisplay.innerText = "Check Console/Email";
            }

            document.getElementById('login-step-contact').classList.add('hidden');
            document.getElementById('login-step-otp').classList.remove('hidden');
            showToast("Code dispatched successfully!", "success");
        } else {
            showToast(data.error || "Failed to send verification code", "error");
        }
    } catch (err) {
        console.error(err);
        showToast("Connection error. Please try again.", "error");
    }
}

async function verifyOtp() {
    const otpVal = document.getElementById('login-otp-input').value.trim();
    if (!otpVal) return;

    try {
        const res = await fetch('/api/auth/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contact: state.selectedContact, otp: otpVal })
        });
        const data = await res.json();
        if (res.ok) {
            if (data.exists && data.user) {
                if (state.loginMode === 'partner' && !data.user.partnerApproved) {
                    showToast("Acest cont nu este înregistrat sau aprobat ca partener! / This account is not registered or approved as a partner!", "error");
                    showLoginError("Acest cont nu are drepturi de administrator de salon! / This account does not have salon admin privileges!");
                    return;
                }

                localStorage.setItem('user_contact', data.user.contact);
                state.user = data.user;
                showToast("Welcome back!", "success");

                if (state.loginMode === 'partner') {
                    navigateTo('partner');
                } else {
                    navigateTo('home');
                }
            } else {
                if (state.loginMode === 'partner') {
                    showToast("Contul nu există. Înscrie-te mai întâi ca și client standard! / Account does not exist. Sign in on client side first!", "error");
                    showLoginError("Partenerii trebuie înregistrați manual. Te rugăm să trimiți o cerere de partener din portalul clienți!");
                } else {
                    document.getElementById('login-step-otp').classList.add('hidden');
                    document.getElementById('login-step-register').classList.remove('hidden');
                    renderAvatarPresets();
                }
            }
        } else {
            showToast("Invalid verification code", "error");
        }
    } catch (err) {
        showToast("Verification failed", "error");
    }
}

function renderAvatarPresets() {
    const grid = document.getElementById('avatar-presets-grid');
    grid.innerHTML = presetAvatars.map((av, index) => {
        const isSelected = selectedRegAvatar === av;
        return `
                  <button type="button" onclick="selectRegAvatar('${av}')" class="relative w-10 h-10 mx-auto rounded-full overflow-hidden border-2 transition-all cursor-pointer ${
            isSelected ? 'border-blue-600 scale-105 shadow-sm' : 'border-transparent opacity-60 hover:opacity-100'
        }">
                      <img src="${av}" referrerpolicy="no-referrer" class="w-full h-full object-cover">
                  </button>
              `;
    }).join('');
}

function selectRegAvatar(avUrl) {
    selectedRegAvatar = avUrl;
    renderAvatarPresets();
}

async function submitUserRegistration() {
    const first = document.getElementById('reg-firstname-input').value.trim();
    const last = document.getElementById('reg-lastname-input').value.trim();
    const phone = document.getElementById('reg-phone-input').value.trim();
    const birthday = document.getElementById('reg-birthday-input').value;

    if (!first || !last || !phone) {
        showToast("Please fill in first name, surname, and phone number.", "error");
        return;
    }

    if (birthday) {
        const birthDate = new Date(birthday);
        const minDate = new Date('1900-01-01');
        const maxDate = new Date();
        minDate.setHours(0,0,0,0);
        maxDate.setHours(23,59,59,999);

        if (birthDate < minDate || birthDate > maxDate || isNaN(birthDate.getTime())) {
            showToast("Data nașterii trebuie să fie între 1900 și astăzi! / Birthday must be between 1900 and today!", "error");
            return;
        }
    }

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                contact: state.selectedContact,
                firstName: first,
                lastName: last,
                phone: phone,
                birthday: birthday,
                photoUrl: selectedRegAvatar
            })
        });
        const data = await res.json();
        if (res.ok && data.user) {
            localStorage.setItem('user_contact', data.user.contact);
            state.user = data.user;
            showToast("Account initialized successfully!", "success");
            navigateTo('home');
        } else {
            showToast(data.error || "Profile creation failed", "error");
        }
    } catch (err) {
        console.error(err);
        showToast("Connection error during registration", "error");
    }
}

function showLoginError(msg) {
    const errorDiv = document.getElementById('login-error');
    errorDiv.innerText = msg;
    errorDiv.classList.remove('hidden');
}

function logout() {
    localStorage.removeItem('user_contact');
    state.user = null;

    document.getElementById('login-contact-input').value = '';
    document.getElementById('login-otp-input').value = '';

    document.getElementById('reg-firstname-input').value = '';
    document.getElementById('reg-lastname-input').value = '';
    document.getElementById('reg-phone-input').value = '';
    document.getElementById('reg-birthday-input').value = '';

    document.getElementById('login-error').classList.add('hidden');

    document.getElementById('login-step-contact').classList.remove('hidden');
    document.getElementById('login-step-otp').classList.add('hidden');
    document.getElementById('login-step-register').classList.add('hidden');

    state.selectedContact = '';
    state.selectedTime = null;
    showToast("Signed out successfully", "success");
    navigateTo('login');
}

async function openSalonDetail(salonId) {
    try {
        const res = await fetch(`/api/salon/${salonId}`);
        if (res.ok) {
            const data = await res.json();
            state.selectedSalon = data.salon;
            state.selectedSalonServices = data.services;
            state.selectedSalonBookings = data.bookings;
            state.selectedSalonBlockedSlots = data.blockedSlots || [];

            const description = state.currentLang === 'ro' ? data.salon.descriptionRo : data.salon.descriptionEn;

            // Hydrate the DOM elements
            document.getElementById('salon-hero-image').src = data.salon.imageUrl;
            document.getElementById('salon-hero-name').innerText = data.salon.name;
            document.getElementById('salon-hero-desc').innerText = description;
            document.getElementById('salon-hero-rating').innerText = data.salon.rating.toFixed(1);

            // ⭐ SAFE BINDING: Safely catch the button and assign the navigation route parameter
            const reviewsBtn = document.getElementById('salon-view-reviews-btn');
            if (reviewsBtn) {
                reviewsBtn.setAttribute('onclick', 'fetchSalonReviewsFeed()');
            }
            // Render the services list layout
            const servicesList = document.getElementById('salon-services-list');
            servicesList.innerHTML = data.services.map(s => {
                const srvName = state.currentLang === 'ro' ? s.nameRo : s.nameEn;
                return `
            <div class="bg-white rounded-2xl p-6 border border-slate-200 flex items-center justify-between gap-4">
                <div>
                    <h5 class="font-bold text-slate-900 text-lg">${srvName}</h5>
                    <div class="flex items-center gap-4 text-xs font-semibold text-slate-500 mt-2">
                        <div class="flex items-center gap-1"><i data-lucide="clock" class="w-4 h-4 text-slate-400"></i> ${s.durationMinutes} min</div>
                        <div class="flex items-center gap-1"><i data-lucide="circle-dollar-sign" class="w-4 h-4 text-slate-400"></i> ${s.price} RON</div>
                    </div>
                </div>
                <button onclick="openBookingModal(${s.id})" class="px-6 py-3 rounded-full bg-blue-600 text-white font-bold text-sm">Book Now</button>
            </div>
        `;
            }).join('');

            // Route the viewport to the salon profile view card
            navigateTo('salon');
        }
    } catch (err) {
        console.error("Error opening salon details:", err);
        showToast("Could not load salon workspace details.", "error");
    }
}

async function cancelBooking(bookingId) {
    const contact = localStorage.getItem('user_contact') || "";
    if (!confirm("Are you sure you want to cancel this appointment?")) return;
    try {
        const res = await fetch(`/api/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: { 'X-User-Contact': contact }
        });
        if (res.ok) {
            showToast("Appointment successfully canceled", "success");
            fetchHomeData();
        } else {
            showToast("Failed to cancel appointment", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

function openBookingModal(serviceId) {
    const service = state.selectedSalonServices.find(s => s.id === serviceId);
    if (!service) return;
    state.selectedService = service;
    state.selectedDate = document.getElementById('booking-date').value;
    state.selectedTime = null;

    document.getElementById('booking-modal').classList.remove('hidden');
    renderBookingHours();
}

function closeBookingModal() {
    document.getElementById('booking-modal').classList.add('hidden');
}
// Navigates directly from the salon detail profile to the reviews view window
function viewSalonReviewsPage() {
    if (!state.selectedSalon) return;
    navigateTo('reviews');
}

// REST Client: Fetches dataset asynchronously matching active Salon ID matching Controller mapping
async function fetchSalonReviewsFeed() {
    // 1. Fail gracefully if no salon is active
    if (!state.selectedSalon) {
        navigateTo('home');
        return;
    }

    const salonId = state.selectedSalon.id;
    const feedContainer = document.getElementById('salon-reviews-feed-container');

    // 2. Wrap DOM updates in safe null-checks so a missing element won't break execution
    const titleEl = document.getElementById('reviews-salon-title');
    if (titleEl) titleEl.innerText = `${state.selectedSalon.name} Reviews`;

    const scoreEl = document.getElementById('reviews-aggregate-score');
    if (scoreEl) scoreEl.innerText = (state.selectedSalon.rating || 5.0).toFixed(1);

    const countEl = document.getElementById('reviews-total-count');
    if (countEl) countEl.innerText = `Based on ${state.selectedSalon.reviewCount || 0} entries`;

    const backBtn = document.getElementById('reviews-back-btn');
    if (backBtn) backBtn.setAttribute('onclick', `openSalonDetail(${salonId})`);

    // First transition to reviews page view state
    navigateTo('reviews');

    if (!feedContainer) return;
    feedContainer.innerHTML = `<div class="col-span-full py-8 text-center text-xs font-bold text-slate-400">Loading feedback parameters...</div>`;

    try {
        const res = await fetch(`/api/salon/${salonId}/reviews`);
        if (res.ok) {
            const data = await res.json();

            // Adapt dynamically to both an object wrapper or a direct array return from your Controller
            state.selectedSalonReviews = Array.isArray(data) ? data : (data.reviews || []);

            // 3. Fallback message if database table is completely empty
            if (state.selectedSalonReviews.length === 0) {
                feedContainer.innerHTML = `
              <div class="col-span-full py-12 text-center text-slate-400 text-xs font-semibold bg-white border border-slate-200 rounded-3xl p-6">
                  Nu există recenzii scrise pentru acest salon momentan. <br>
                  <span class="text-[11px] font-normal text-slate-400">No client reviews have been registered for this workspace yet.</span>
              </div>`;
                return;
            }

            // 4. Clean, crash-proof map rendering loop
            feedContainer.innerHTML = state.selectedSalonReviews.map(rev => {
                const reviewerName = rev.name || rev.clientName || rev.reviewerName || "Anonymous Client";
                const reviewText = rev.text || rev.comment || rev.content || "Great service!";
                const reviewStars = rev.stars || rev.rating || 5;
                const rawDate = rev.date || rev.createdAt || "";

                let starsHtml = '';
                for (let i = 1; i <= 5; i++) {
                    const isFilled = i <= reviewStars;
                    starsHtml += `<i data-lucide="star" class="w-3.5 h-3.5 ${isFilled ? 'fill-yellow-500 text-yellow-500' : 'text-slate-200'}"></i>`;
                }

                return `
                  <div class="bg-white border border-slate-200 shadow-2xs rounded-2xl p-5 space-y-3 animate-fade-in">
                    <div class="flex items-center justify-between">
                      <div>
                        <h5 class="font-black text-slate-800 text-xs leading-none">${reviewerName}</h5>
                        <p class="text-[9px] text-slate-400 font-semibold mt-1">${rawDate ? formatFriendlyDate(rawDate) : 'Recently'}</p>
                      </div>
                      <div class="flex items-center gap-0.5 bg-slate-50 border border-slate-100 px-2 py-1 rounded-lg">
                        ${starsHtml}
                      </div>
                    </div>
                    <p class="text-slate-600 text-xs leading-relaxed font-medium italic">"${reviewText}"</p>
                  </div>
                `;
            }).join('');

            lucide.createIcons();
        } else {
            feedContainer.innerHTML = `<div class="col-span-full py-8 text-center text-xs text-red-500 font-bold">API returned an operational error code.</div>`;
        }
    } catch (err) {
        console.error("Reviews rendering crashed:", err);
        feedContainer.innerHTML = `<div class="col-span-full py-8 text-center text-xs text-red-500 font-bold">JavaScript mapping loop encountered a data mismatch error.</div>`;
    }
}

function getIntervalsForDuration(duration) {
    const slots = [];
    let currentMinutes = 9 * 60;
    const endMinutes = 17 * 60;

    while (currentMinutes + duration <= endMinutes) {
        const hours = Math.floor(currentMinutes / 60);
        const mins = currentMinutes % 60;
        const timeString = `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
        slots.push(timeString);
        currentMinutes += duration;
    }
    return slots;
}

function checkOverlap(proposedDate, proposedTimeStr, duration, existingBookings, blockedSlots) {
    const proposedStart = new Date(`${proposedDate}T${proposedTimeStr}:00`);
    const proposedEnd = new Date(proposedStart.getTime() + duration * 60 * 1000);

    for (const b of existingBookings) {
        try {
            const bParts = b.bookingDateTime.split('T');
            if (bParts[0] === proposedDate) {
                const bStart = new Date(b.bookingDateTime);
                const bDuration = b.service?.durationMinutes || 60;
                const bEnd = new Date(bStart.getTime() + bDuration * 60 * 1000);

                if (proposedStart < bEnd && bStart < proposedEnd) {
                    return true;
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    for (const bs of blockedSlots) {
        try {
            const bsParts = bs.dateTime.split('T');
            if (bsParts[0] === proposedDate) {
                const bsStart = new Date(bs.dateTime);
                const bsEnd = new Date(bsStart.getTime() + 60 * 60 * 1000);

                if (proposedStart < bsEnd && bsStart < proposedEnd) {
                    return true;
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    return false;
}

function renderBookingHours() {
    const grid = document.getElementById('booking-hours-grid');
    if (!state.selectedService) return;

    const duration = state.selectedService.durationMinutes;
    const dynamicWorkingHours = getIntervalsForDuration(duration);

    grid.innerHTML = dynamicWorkingHours.map(hour => {
        const isBusy = checkOverlap(state.selectedDate, hour, duration, state.selectedSalonBookings, state.selectedSalonBlockedSlots);
        const isSelected = state.selectedTime === hour;

        return `
                  <button type="button"
                      ${isBusy ? 'disabled' : ''}
                      onclick="selectBookingTime('${hour}')"
                      class="py-3 rounded-xl font-bold text-center text-sm transition-all ${
            isBusy ? 'bg-slate-100 text-slate-300 line-through cursor-not-allowed' :
                isSelected ? 'bg-blue-600 text-white shadow-sm font-black' :
                    'bg-slate-50 text-slate-700 hover:bg-slate-100 border border-slate-200'
        }">
                      ${hour}
                  </button>
              `;
    }).join('');
}

function selectBookingTime(hour) {
    state.selectedTime = hour;
    renderBookingHours();
}

async function submitBooking() {
    if (!state.selectedService || !state.selectedTime) {
        showToast("Please select an available timeslot before booking!", "error");
        return;
    }
    const contact = localStorage.getItem('user_contact') || "";
    const bookingDateTime = `${state.selectedDate}T${state.selectedTime}:00`;

    try {
        const res = await fetch('/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Contact': contact
            },
            body: JSON.stringify({
                salonId: state.selectedSalon.id,
                serviceId: state.selectedService.id,
                bookingDateTime: bookingDateTime
            })
        });
        if (res.ok) {
            showToast("Reservation successfully registered!", "success");
            closeBookingModal();
            navigateTo('home');
        } else {
            const err = await res.json();
            showToast(err.error || "Failed booking", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

async function fetchPartnerWorkspace() {
    const contact = localStorage.getItem('user_contact') || "";
    try {
        const res = await fetch('/api/partner/salons', {
            headers: { 'X-User-Contact': contact }
        });
        const data = await res.json();
        if (res.ok && data.salons.length > 0) {
            state.partnerSalonsList = data.salons;

            if (!state.partnerSalon) {
                state.partnerSalon = data.salons[0];
            }

            const select = document.getElementById('partner-salon-select');
            if (data.salons.length > 1) {
                select.classList.remove('hidden');
                select.innerHTML = data.salons.map(s => `
          <option value="${s.id}" ${state.partnerSalon.id === s.id ? 'selected' : ''}>${s.name}</option>
        `).join('');
            } else {
                select.classList.add('hidden');
            }

            document.getElementById('partner-salon-name').innerText = state.partnerSalon.name + " Workspace";
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        }
    } catch (err) {
        console.error(err);
    }
}

function switchPartnerSalon(salonId) {
    const selected = state.partnerSalonsList.find(s => s.id == salonId);
    if (selected) {
        state.partnerSalon = selected;
        document.getElementById('partner-salon-name').innerText = selected.name + " Workspace";
        showToast(`Switched workspace to: ${selected.name}`, "info");
        fetchPartnerWorkspaceDetails(selected.id);
    }
}

async function fetchPartnerWorkspaceDetails(salonId) {
    const contact = localStorage.getItem('user_contact') || "";
    try {
        const res = await fetch(`/api/partner/salons/${salonId}/bookings`, {
            headers: { 'X-User-Contact': contact }
        });
        const data = await res.json();
        state.partnerServices = data.services;
        state.partnerBookings = data.bookings;
        state.partnerBlockedSlots = data.blockedSlots || [];

        const resC = await fetch('/api/partner/customers', {
            headers: { 'X-User-Contact': contact }
        });
        const dataC = await resC.json();
        state.partnerCustomers = dataC.customers;

        renderPartnerWorkspace();
    } catch (err) {
        console.error(err);
    }
}

function renderPartnerWorkspace() {
    const serviceListDiv = document.getElementById('partner-services-list');
    serviceListDiv.innerHTML = state.partnerServices.map(s => {
        const srvName = state.currentLang === 'ro' ? s.nameRo : s.nameEn;
        return `
              <div class="py-2.5 flex items-center justify-between text-xs">
                  <div>
                      <p class="font-extrabold text-slate-800">${srvName}</p>
                      <p class="text-[10px] text-slate-400 font-bold">${s.durationMinutes} min • <span class="text-blue-600">${s.price} RON</span></p>
                  </div>
                  <button onclick="deleteService(${s.id})" class="p-1 text-slate-400 hover:text-red-500 rounded-lg cursor-pointer">
                      <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                  </button>
              </div>
          `;
    }).join('');

    const customerListDiv = document.getElementById('partner-customers-list');
    customerListDiv.innerHTML = state.partnerCustomers.map(c => `
              <div class="py-2.5 flex items-center justify-between gap-3 text-xs">
                  <div class="flex items-center gap-3">
                      <img src="${c.photoUrl || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150&auto=format&fit=crop'}" referrerpolicy="no-referrer" class="w-8 h-8 rounded-full object-cover border border-slate-200">
                      <div>
                          <h4 class="font-black text-slate-800 text-xs">${c.firstName} ${c.lastName}</h4>
                          <p class="text-[10px] text-slate-400">${c.contact}</p>
                      </div>
                  </div>
                  <button onclick="openPartnerBookingModal('${c.contact}', '${c.firstName} ${c.lastName}')" class="bg-blue-50 text-blue-600 hover:bg-blue-600 hover:text-white font-black text-[10px] uppercase tracking-wider px-3 py-1.5 rounded-lg border border-blue-100/50 transition-all cursor-pointer">
                      Book Appt
                  </button>
              </div>
          `).join('');

    renderBlockSlotHours();

    const bookingsTable = document.getElementById('partner-bookings-table');
    bookingsTable.innerHTML = state.partnerBookings.map(b => {
        const srvName = state.currentLang === 'ro' ? b.service?.nameRo : b.service?.nameEn;
        return `
              <tr class="hover:bg-slate-50/50">
                  <td class="py-3 px-2 font-black text-slate-700">${formatFriendlyDate(b.bookingDateTime)}</td>
                  <td class="py-3 px-2">
                      <p class="font-extrabold text-slate-800">${b.client?.firstName || 'Client'} ${b.client?.lastName || 'Offline'}</p>
                      <p class="text-[10px] text-slate-400 leading-none">${b.userContact}</p>
                  </td>
                  <td class="py-3 px-2">
                      <span class="bg-slate-100 px-2 py-0.5 rounded-md text-slate-700 font-bold">${srvName || 'Service'}</span>
                  </td>
                  <td class="py-3 px-2 text-center">
                      <button onclick="cancelBookingByPartner(${b.id})" class="p-1.5 text-slate-400 hover:text-red-500 rounded-lg cursor-pointer">
                          <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                      </button>
                  </td>
              </tr>
          `;
    }).join('');

    lucide.createIcons();
}

function renderBlockSlotHours() {
    const dateVal = document.getElementById('block-slot-date').value;
    const grid = document.getElementById('block-slots-hours-grid');

    const activeBookingTimes = [];
    state.partnerBookings.forEach(b => {
        const parts = b.bookingDateTime.split('T');
        if (parts[0] === dateVal) {
            activeBookingTimes.push(parts[1].substring(0, 5));
        }
    });

    const blockedTimes = [];
    state.partnerBlockedSlots.forEach(bs => {
        const parts = bs.dateTime.split('T');
        if (parts[0] === dateVal) {
            blockedTimes.push(parts[1].substring(0, 5));
        }
    });

    grid.innerHTML = workingHours.map(hour => {
        const isBooked = activeBookingTimes.includes(hour);
        const isBlocked = blockedTimes.includes(hour);

        return `
                  <button type="button"
                      onclick="toggleBlockHour('${hour}', ${isBooked})"
                      class="py-2.5 rounded-xl font-bold text-center text-xs transition-all border ${
            isBooked ? 'bg-rose-50 border-rose-100 text-rose-700 cursor-not-allowed opacity-60' :
                isBlocked ? 'bg-amber-100 border-amber-200 text-amber-800' :
                    'bg-emerald-50 border-emerald-100 text-emerald-800 hover:bg-emerald-100'
        }">
                      <span>${hour}</span>
                      <p class="text-[8px] uppercase font-black opacity-80 mt-0.5">${isBooked ? 'Booked' : isBlocked ? 'Blocked' : 'Free'}</p>
                  </button>
              `;
    }).join('');
}

async function toggleBlockHour(hour, isBooked) {
    if (isBooked) return;
    const dateVal = document.getElementById('block-slot-date').value;
    const dateTime = `${dateVal}T${hour}`;
    const contact = localStorage.getItem('user_contact') || "";
    try {
        const res = await fetch(`/api/partner/salons/${state.partnerSalon.id}/blocked-slots/toggle`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-user-contact': contact
            },
            body: JSON.stringify({ dateTime: dateTime })
        });
        if (res.ok) {
            showToast("Slot availability updated!", "success");
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to update slot status", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

async function blockWholeDay() {
    const dateVal = document.getElementById('block-slot-date').value;
    if (!dateVal) {
        showToast("Please select a date first.", "error");
        return;
    }

    const contact = localStorage.getItem('user_contact') || "";
    if (!confirm(`Are you sure you want to block the entire day on ${dateVal}?`)) return;

    try {
        const res = await fetch(`/api/partner/salons/${state.partnerSalon.id}/blocked-slots/bulk`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-user-contact': contact
            },
            body: JSON.stringify({
                dates: [dateVal],
                action: 'block',
                timeSlots: workingHours
            })
        });

        if (res.ok) {
            showToast(`Entire day on ${dateVal} has been successfully closed/blocked!`, "success");
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to block entire day.", "error");
        }
    } catch (err) {
        console.error(err);
        showToast("Error blocking entire day.", "error");
    }
}

function openCustomBlockModal() {
    const todayStr = new Date().toISOString().split('T')[0];
    document.getElementById('custom-block-start-date').value = todayStr;
    document.getElementById('custom-block-end-date').value = todayStr;
    document.getElementById('custom-block-start-time').value = "09:00";
    document.getElementById('custom-block-end-time').value = "10:00";

    document.getElementById('custom-block-modal').classList.remove('hidden');
}

function closeCustomBlockModal() {
    document.getElementById('custom-block-modal').classList.add('hidden');
}

async function submitCustomBlock() {
    const startDateStr = document.getElementById('custom-block-start-date').value;
    const endDateStr = document.getElementById('custom-block-end-date').value;
    const startTimeStr = document.getElementById('custom-block-start-time').value;
    const endTimeStr = document.getElementById('custom-block-end-time').value;

    if (!startDateStr || !endDateStr || !startTimeStr || !endTimeStr) {
        showToast("Please fill in all date and time fields.", "error");
        return;
    }

    const start = new Date(startDateStr);
    const end = new Date(endDateStr);
    if (start > end) {
        showToast("Start date must be before or equal to End date.", "error");
        return;
    }

    const [sh, sm] = startTimeStr.split(':').map(Number);
    const [eh, em] = endTimeStr.split(':').map(Number);
    const startMin = sh * 60 + sm;
    const endMin = eh * 60 + em;

    if (startMin >= endMin) {
        showToast("Start time must be strictly before End time.", "error");
        return;
    }

    const contact = localStorage.getItem('user_contact') || "";

    const datesToBlock = [];
    let currDate = new Date(start);
    while (currDate <= end) {
        datesToBlock.push(currDate.toISOString().split('T')[0]);
        currDate.setDate(currDate.getDate() + 1);
    }

    const timeslotsToBlock = [];
    let currMin = startMin;
    while (currMin < endMin) {
        const h = Math.floor(currMin / 60);
        const m = currMin % 60;
        const timeStr = `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
        timeslotsToBlock.push(timeStr);
        currMin += 60;
    }

    try {
        const res = await fetch(`/api/partner/salons/${state.partnerSalon.id}/blocked-slots/bulk`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-user-contact': contact
            },
            body: JSON.stringify({
                dates: datesToBlock,
                action: 'block',
                timeSlots: timeslotsToBlock
            })
        });

        if (res.ok) {
            showToast("Custom availability blocks applied successfully!", "success");
            closeCustomBlockModal();
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to apply custom blocks.", "error");
        }
    } catch (err) {
        console.error(err);
        showToast("Error processing request.", "error");
    }
}

async function addService(e) {
    e.preventDefault();
    const nameEn = document.getElementById('service-name-en').value.trim();
    const nameRo = document.getElementById('service-name-ro').value.trim();
    const price = document.getElementById('service-price').value.trim();
    const duration = document.getElementById('service-duration').value.trim();
    const contact = localStorage.getItem('user_contact') || "";
    try {
        const res = await fetch(`/api/partner/salons/${state.partnerSalon.id}/services`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-user-contact': contact
            },
            body: JSON.stringify({
                nameEn: nameEn,
                nameRo: nameRo,
                price: parseInt(price),
                durationMinutes: parseInt(duration)
            })
        });
        if (res.ok) {
            showToast("Service successfully added!", "success");
            document.getElementById('service-name-en').value = '';
            document.getElementById('service-name-ro').value = '';
            document.getElementById('service-price').value = '';
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to register service", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

async function deleteService(serviceId) {
    if (!confirm("Are you sure you want to remove this service?")) return;
    try {
        const res = await fetch(`/api/partner/services/${serviceId}`, {
            method: 'DELETE'
        });
        if (res.ok) {
            showToast("Service removed from catalog", "success");
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to remove service", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

async function cancelBookingByPartner(bookingId) {
    const contact = localStorage.getItem('user_contact') || "";
    if (!confirm("Are you sure you want to cancel this booking?")) return;
    try {
        const res = await fetch(`/api/bookings/${bookingId}`, {
            method: 'DELETE',
            headers: { 'x-user-contact': contact }
        });
        if (res.ok) {
            showToast("Appointment successfully canceled", "success");
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            showToast("Failed to cancel appointment", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

function openPartnerBookingModal(clientContact, clientName) {
    if (state.partnerServices.length === 0) {
        showToast("Vă rugăm să adăugați servicii mai întâi! / Please add services to your salon first!", "error");
        return;
    }

    state.selectedPartnerClientContact = clientContact;
    document.getElementById('partner-booking-client-name').innerText = clientName;

    const select = document.getElementById('partner-booking-service-select');
    select.innerHTML = state.partnerServices.map(s => {
        const srvName = state.currentLang === 'ro' ? s.nameRo : s.nameEn;
        return `<option value="${s.id}">${srvName} (${s.durationMinutes} min)</option>`;
    }).join('');

    state.selectedPartnerService = state.partnerServices[0];
    state.selectedPartnerTime = null;

    const todayStr = new Date().toISOString().split('T')[0];
    document.getElementById('partner-booking-date').value = todayStr;
    state.selectedPartnerDate = todayStr;

    document.getElementById('partner-booking-modal').classList.remove('hidden');
    renderPartnerBookingHours();
}

function closePartnerBookingModal() {
    document.getElementById('partner-booking-modal').classList.add('hidden');
}

function onPartnerBookingServiceChange(serviceId) {
    state.selectedPartnerService = state.partnerServices.find(s => s.id == serviceId);
    state.selectedPartnerTime = null;
    renderPartnerBookingHours();
}

function renderPartnerBookingHours() {
    const grid = document.getElementById('partner-booking-hours-grid');
    if (!state.selectedPartnerService) return;

    const duration = state.selectedPartnerService.durationMinutes;
    const dynamicWorkingHours = getIntervalsForDuration(duration);

    grid.innerHTML = dynamicWorkingHours.map(hour => {
        const isBusy = checkOverlap(state.selectedPartnerDate, hour, duration, state.partnerBookings, state.partnerBlockedSlots);
        const isSelected = state.selectedPartnerTime === hour;

        return `
                  <button type="button"
                      ${isBusy ? 'disabled' : ''}
                      onclick="selectPartnerBookingTime('${hour}')"
                      class="py-3 rounded-xl font-bold text-center text-sm transition-all ${
            isBusy ? 'bg-slate-100 text-slate-300 line-through cursor-not-allowed' :
                isSelected ? 'bg-blue-600 text-white shadow-sm font-black' :
                    'bg-slate-50 text-slate-700 hover:bg-slate-100 border border-slate-200'
        }">
                      ${hour}
                  </button>
              `;
    }).join('');
}

function selectPartnerBookingTime(hour) {
    state.selectedPartnerTime = hour;
    renderPartnerBookingHours();
}

async function submitPartnerBooking() {
    if (!state.selectedPartnerService || !state.selectedPartnerTime) {
        showToast("Please select an available timeslot before booking!", "error");
        return;
    }
    const contact = localStorage.getItem('user_contact') || "";
    const bookingDateTime = `${state.selectedPartnerDate}T${state.selectedPartnerTime}:00`;

    try {
        const res = await fetch(`/api/partner/salons/${state.partnerSalon.id}/bookings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-user-contact': contact
            },
            body: JSON.stringify({
                serviceId: state.selectedPartnerService.id,
                bookingDateTime: bookingDateTime,
                clientContact: state.selectedPartnerClientContact
            })
        });
        if (res.ok) {
            showToast("Reservation successfully registered!", "success");
            closePartnerBookingModal();
            fetchPartnerWorkspaceDetails(state.partnerSalon.id);
        } else {
            const err = await res.json();
            showToast(err.error || "Failed booking", "error");
        }
    } catch (err) {
        console.error(err);
    }
}

function formatFriendlyDate(rawDateStr) {
    try {
        const parts = rawDateStr.split('T');
        if (parts.length < 2) return rawDateStr;
        const [datePart, timePart] = parts;
        const [y, m, d] = datePart.split('-');
        const [hour, min] = timePart.split(':');
        return `${d}/${m}/${y} ${hour}:${min}`;
    } catch (e) {
        return rawDateStr;
    }
}

let selectedProfileEditAvatar = '';

function openProfileWorkspace() {
    if (!state.user) return;

    document.getElementById('profile-error').classList.add('hidden');
    document.getElementById('profile-firstname-input').value = state.user.firstName || '';
    document.getElementById('profile-lastname-input').value = state.user.lastName || '';
    document.getElementById('profile-phone-input').value = state.user.phone || '';
    document.getElementById('profile-birthday-input').value = state.user.birthday || '';

    selectedProfileEditAvatar = state.user.photoUrl || presetAvatars[0];
    document.getElementById('profile-edit-avatar-preview').src = selectedProfileEditAvatar;

    renderProfileAvatarPresets();
}

function renderProfileAvatarPresets() {
    const grid = document.getElementById('profile-avatar-presets-grid');
    grid.innerHTML = presetAvatars.map(av => {
        const isSelected = selectedProfileEditAvatar === av;
        return `
  <button type="button" onclick="selectProfileEditAvatar('${av}')" class="relative w-9 h-10 mx-auto rounded-full overflow-hidden border-2 transition-all cursor-pointer ${
            isSelected ? 'border-blue-600 scale-105 shadow-sm' : 'border-transparent opacity-60 hover:opacity-100'
        }">
    <img src="${av}" referrerpolicy="no-referrer" class="w-full h-full object-cover">
  </button>
`;
    }).join('');
}

function selectProfileEditAvatar(avUrl) {
    selectedProfileEditAvatar = avUrl;
    document.getElementById('profile-edit-avatar-preview').src = avUrl;
    renderProfileAvatarPresets();
}

async function updateProfileSettings(e) {
    e.preventDefault();
    const errorDiv = document.getElementById('profile-error');
    errorDiv.classList.add('hidden');

    const payload = {
        contact: state.user.contact,
        firstName: document.getElementById('profile-firstname-input').value.trim(),
        lastName: document.getElementById('profile-lastname-input').value.trim(),
        phone: document.getElementById('profile-phone-input').value.trim(),
        birthday: document.getElementById('profile-birthday-input').value,
        photoUrl: selectedProfileEditAvatar
    };

    try {
        const res = await fetch('/api/users/profile/update', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (res.ok) {
            state.user = data.user;
            showToast("Profil actualizat cu succes! / Profile updated successfully!", "success");
            navigateTo('home');
        } else {
            errorDiv.innerText = data.error || "Profile adjustment failed.";
            errorDiv.classList.remove('hidden');
            showToast("Eroare la salvare / Modification tracking rejected.", "error");
        }
    } catch (err) {
        console.error(err);
        showToast("Connection error encountered.", "error");
    }
}