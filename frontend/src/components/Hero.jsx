function Hero({ selectedMode, onSelectMode }) {
  return (
    <section className="hero-text">
      <p className="badge">next-gen social discovery</p>

      <h1>
        Meet people instantly.
        <span> Text, video, and real conversations.</span>
      </h1>

      <p className="description">
        Matchly connects you with new people through fast text and video chat
        in a cleaner, safer, and more modern experience.
      </p>

      <div className="hero-buttons">
        <button
          className={
            selectedMode === "video"
              ? "primary-button"
              : "secondary-button"
          }
          onClick={() => onSelectMode("video")}
        >
          Start Video Chat
        </button>

        <button
          className={
            selectedMode === "text"
              ? "primary-button"
              : "secondary-button"
          }
          onClick={() => onSelectMode("text")}
        >
          Start Text Chat
        </button>
      </div>

      <p className="privacy-text">
        No signup required for guest mode. Fast, simple, and safety-focused.
      </p>
    </section>
  );
}

export default Hero;