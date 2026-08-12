import { useState } from "react";

import Hero from "../components/Hero";
import PreviewCard from "../components/PreviewCard";
import Features from "../components/Features";

function HomePage() {
  const [chatMode, setChatMode] = useState("video");

  return (
    <>
      <main className="hero">
        <Hero
          selectedMode={chatMode}
          onSelectMode={setChatMode}
        />

        <PreviewCard chatMode={chatMode} />
      </main>

      <Features />
    </>
  );
}

export default HomePage;